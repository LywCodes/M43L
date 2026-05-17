package ita.job;

import ita.dto.EmailBatchDto;
import ita.dto.EmailTaskDto;
import ita.entity.CampaignDetail;
import ita.enumeration.CampaignDetailStatus;
import ita.service.CampaignDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CampaignBatchWriter implements ItemWriter<EmailBatchDto> {

    private final CampaignDetailService campaignDetailService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String emailTopic;

    public CampaignBatchWriter(@Lazy CampaignDetailService campaignDetailService,
                               KafkaTemplate<String, Object> kafkaTemplate,
                               @Value("${spring.kafka.topic.email}") String emailTopic) {

        this.campaignDetailService = campaignDetailService;
        this.kafkaTemplate = kafkaTemplate;
        this.emailTopic = emailTopic;
    }

    @Override
    public void write(Chunk<? extends EmailBatchDto> chunk){
        List<CampaignDetail> detailsToSave = new ArrayList<>();

        for (EmailBatchDto item : chunk.getItems()) {
            detailsToSave.add(item.getCampaignDetail());
        }

        campaignDetailService.saveAll(detailsToSave);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                List<CampaignDetail> failedDetails = new ArrayList<>();

                for (EmailBatchDto item : chunk.getItems()) {
                    if (!item.isInvalidEmail()) {
                        String trackerId = item.getCampaignDetail().getTrackerId();
                        EmailTaskDto taskDto = item.getEmailTaskDto();
                        taskDto.setCampaignDetailId(item.getCampaignDetail().getId());

                        try {
                            kafkaTemplate.send(emailTopic, trackerId, taskDto).get();
                        } catch (InterruptedException e) {
                            log.error("Kafka interrupted while waiting Kafka to {}: {} ", trackerId, e.getMessage());
                            Thread.currentThread().interrupt();

                            throw new RuntimeException("Proses Batch interrupted", e);
                        } catch (Exception e) {
                            log.error("failed send to Kafka for {}: {}", item.getEmailTaskDto().getRecipientEmail(), e.getMessage());

                            CampaignDetail failedDetail = item.getCampaignDetail();

                            failedDetail.setStatus(CampaignDetailStatus.SOFT_BOUNCED);
                            failedDetail.setSoftBouncedAt(System.currentTimeMillis());
                            failedDetail.setSentAt(0L);
                            failedDetails.add(failedDetail);
                        }
                    }

                    if (!failedDetails.isEmpty()) {
                        campaignDetailService.saveAllFailed(failedDetails);
                    }
                }
            }

        });
    }
}