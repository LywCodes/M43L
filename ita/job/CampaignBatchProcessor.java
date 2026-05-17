package ita.job;

import ita.dto.EmailBatchDto;
import ita.dto.EmailTaskDto;
import ita.entity.CampaignDetail;
import ita.entity.CampaignHeader;
import ita.entity.Contact;
import ita.enumeration.CampaignDetailStatus;
import ita.repository.CampaignHeaderRepository;
import ita.service.ContactAttributeService;
import ita.util.ContentUtil;
import ita.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static ita.util.ContentUtil.decodeBase64;

@Component
@StepScope
@Slf4j
public class CampaignBatchProcessor implements ItemProcessor<Contact, EmailBatchDto> {

    @Value("${tracker.url}")
    private String trackerUrl;

    @Value("${unsubscribe.url}")
    private String unsubscribeUrl;

    private String decodedHtmlTemplate;
    private CampaignHeader campaignHeader;
    private Map<UUID, Map<String, String>> attributesMap;

    public CampaignBatchProcessor(CampaignHeaderRepository campaignHeaderRepository,
                                  ContactAttributeService contactAttributeService,
                                  @Value("#{jobParameters['campaignHeaderId']}") String campaignHeaderId,
                                  @Value("#{jobParameters['contactGroupId']}") String contactGroupId) {

        if (campaignHeaderId != null && contactGroupId != null) {
            this.campaignHeader = campaignHeaderRepository.findById(UUID.fromString(campaignHeaderId)).orElseThrow();
            this.attributesMap = contactAttributeService.getAttributesMapByGroupId(UUID.fromString(contactGroupId));
            this.decodedHtmlTemplate = decodeBase64(campaignHeader.getContent().getHtml());
        }else {
            decodedHtmlTemplate = "";
        }
    }

    @Override
    public EmailBatchDto process(Contact contact)  {
        // log.info("Processing Contact ID{}, email: {} by {}",contact.getId() ,contact.getEmail(), Thread.currentThread());
        String email = contact.getEmail();
        String trackerId = email.split("@")[0] + UUID.randomUUID();


        if (!EmailUtil.isEmailAddressValid(email)) {
            CampaignDetail failedDetail = CampaignDetail.builder()
                    .campaignHeader(campaignHeader)
                    .contact(contact)
                    .trackerId(trackerId)
                    .status(CampaignDetailStatus.SOFT_BOUNCED)
                    .softBouncedAt(System.currentTimeMillis())
                    .build();
            return EmailBatchDto.builder()
                    .campaignDetail(failedDetail)
                    .invalidEmail(true)
                    .build();
        }

        Map<String, String> finalAttributes = new HashMap<>();

        finalAttributes.put("email", contact.getEmail());
        finalAttributes.put("name", contact.getName());

        Map<String, String> contactParams = attributesMap.get(contact.getId());

        if (contactParams != null) {
            finalAttributes.putAll(contactParams);
        }

        String attributeHtml = ContentUtil.replaceKeys(decodedHtmlTemplate, finalAttributes);

        String finalHtml = getEmailBodyWithTracker(attributeHtml, trackerId);

        byte[] attachmentData = null;
        String attachmentName = null;

        if (campaignHeader.getAttachment() != null) {
            attachmentData = campaignHeader.getAttachment().getFile();
            attachmentName = campaignHeader.getAttachment().getName() + ".pdf";
        }

        CampaignDetail campaignDetail = CampaignDetail.builder()
                .campaignHeader(campaignHeader)
                .contact(contact)
                .trackerId(trackerId)
                .status(CampaignDetailStatus.QUEUED)
                .sentAt(System.currentTimeMillis())
                .build();

        EmailTaskDto emailTask = EmailTaskDto.builder()
                .senderEmail(campaignHeader.getSender().getEmail())
                .recipientEmail(email)
                .subject(campaignHeader.getSubject())
                .htmlContent(finalHtml)
                .attachmentFile(attachmentData)
                .attachmentName(attachmentName)
                .build();

        return EmailBatchDto.builder()
                .campaignDetail(campaignDetail)
                .emailTaskDto(emailTask)
                .invalidEmail(false)
                .build();
    }

    private String getEmailBodyWithTracker(String body, String pixelId) {
        String imgUrl = trackerUrl + pixelId;

        String oldString = "href=\"\"";
        String newString = "href=\"" + unsubscribeUrl + pixelId + "\"";

        String updatedBody = body.replace(oldString, newString);

        return """
    <html>
      <body>
        %s
        <img src="%s" loading="lazy" width="2" height="1">
      </body>
    </html>
    """.formatted(updatedBody, imgUrl);
    }
}
