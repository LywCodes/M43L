package ita.service;

import ita.dto.CampaignDetailResponseDto;
import ita.dto.CampaignHistorySearchCriteria;
import ita.dto.EmailTaskDto;
import ita.entity.*;
import ita.enumeration.CampaignDetailStatus;
import ita.exception.NotFoundException;
import ita.repository.CampaignDetailRepository;
import ita.specification.CampaignDetailSpecification;
import lombok.extern.slf4j.Slf4j;


import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static ita.enumeration.EntityType.CAMPAIGN_DETAIL_TYPE;

@Service
@Slf4j
public class CampaignDetailService {
    private final ContactAttributeService contactAttributeService;
    private final CampaignDetailRepository campaignDetailRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${tracker.url}")
    private String trackerUrl;

    @Value("${unsubscribe.url}")
    private String unsubscribeUrl;

    @Value("${spring.kafka.topic.email}")
    private String emailTopic;

    @Value("${param.header}")
    private final String[] paramHeaders;

    @Value("${param.seeding}")
    private final String[] paramSeeding;

    private final JobOperator jobOperator;
    private final Job campaignBlastJob;

    public CampaignDetailService(CampaignDetailRepository campaignDetailRepository,
                                 KafkaTemplate<String, Object> kafkaTemplate,
                                 String[] paramHeaders,
                                 String[] paramSeeding,
                                 ContactAttributeService contactAttributeService,
                                 JobOperator jobOperator,
                                 @Qualifier("campaignBlastJob") Job campaignBlastJob
                                ) {
        this.campaignDetailRepository = campaignDetailRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.paramHeaders = paramHeaders;
        this.paramSeeding = paramSeeding;
        this.contactAttributeService = contactAttributeService;
        this.jobOperator = jobOperator;
        this.campaignBlastJob = campaignBlastJob;
    }

    public Page<CampaignDetailResponseDto> findAllCampaignDetail(CampaignHistorySearchCriteria searchCriteria) {
        Sort sort = Sort.by("campaignHeader." + searchCriteria.getParam());
        Pageable pageable = PageRequest.of(
                searchCriteria.getPage(),
                searchCriteria.getSize(),
                searchCriteria.getType().equalsIgnoreCase("desc") ? sort.descending() : sort.ascending()
        );

        UUID paramId = Optional.ofNullable(searchCriteria.getId())
                .filter(id -> !id.isBlank())
                .map(UUID::fromString)
                .orElse(null);

        Specification<CampaignDetail> campaignDetailSpecification = Specification.allOf(CampaignDetailSpecification.idLike(paramId))
                .and(CampaignDetailSpecification.nameLike(searchCriteria.getName()))
                .and(CampaignDetailSpecification.senderEmailLike(searchCriteria.getSenderEmail()))
                .and(CampaignDetailSpecification.sentAtWithinDateRange(searchCriteria.getStartDate(), searchCriteria.getEndDate()))
                .and(CampaignDetailSpecification.statusEqual(searchCriteria.getStatus()));

        Page<CampaignDetail> campaignDetails = campaignDetailRepository.findAll(campaignDetailSpecification, pageable);

        List<CampaignDetailResponseDto> campaignDetailResponseDtos = campaignDetails.getContent().stream()
                .map(campaignDetail -> CampaignDetailResponseDto.builder()
                        .id(campaignDetail.getId())
                        .name(campaignDetail.getCampaignHeader().getName())
                        .status(campaignDetail.getStatus())
                        .sender(campaignDetail.getCampaignHeader().getSender().getName())
                        .contact(campaignDetail.getContact().getEmail())
                        .sentAt(campaignDetail.getSentAt())
                        .openedAt(campaignDetail.getOpenedAt())
                        .clickedAt(campaignDetail.getClickedAt())
                        .softBouncedAt(campaignDetail.getSoftBouncedAt())
                        .hardBouncedAt(campaignDetail.getHardBouncedAt())
                        .unsubscribedAt(campaignDetail.getUnsubscribedAt())
                        .build())
                .toList();

        return new PageImpl<>(campaignDetailResponseDtos, pageable, campaignDetails.getTotalElements());
    }

    public void createCampaign(CampaignHeader campaignHeader) {
//        processEmailCommunication(campaignHeader);
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("campaignHeaderId", campaignHeader.getId().toString())
                    .addString("contactGroupId", campaignHeader.getContactGroup().getId().toString())
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            CompletableFuture.runAsync(() -> {
                try {
                    jobOperator.start(campaignBlastJob, jobParameters);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }, Executors.newVirtualThreadPerTaskExecutor());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("failed to prepare campaign batch job", e);
        }
    }

    public Slice<CampaignDetail> findSliceByTimeRange(Long startDate, Long endDate, Pageable pageable){
        return campaignDetailRepository.findSliceByTimeRange(startDate, endDate, pageable);
    }

    public long countByStatusAndName(CampaignDetailStatus status, String name) {
        Specification<CampaignDetail> campaignDetailSpecification = Specification.allOf(CampaignDetailSpecification.statusEqual(status))
                .and(CampaignDetailSpecification.nameLike(name));

        return campaignDetailRepository.count(campaignDetailSpecification);
    }

    public CampaignDetail findByTrackerId(String trackerId) {
        Optional<CampaignDetail> campaignDetailFromDb = campaignDetailRepository.findByTrackerId(trackerId);

        if (campaignDetailFromDb.isEmpty()) {
            throw new NotFoundException(CAMPAIGN_DETAIL_TYPE, "Tracker Id", trackerId);
        }

        return campaignDetailFromDb.get();
    }

    public void updateCampaign(CampaignDetail campaignDetail) {
        campaignDetailRepository.save(campaignDetail);
    }

    private String decodeBase64(String base64String) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);

        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

}