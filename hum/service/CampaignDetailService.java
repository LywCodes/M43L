package ita.service;

import ita.config.EmailServerConfig;
import ita.dto.CampaignDetailResponseDto;
import ita.dto.CampaignHistorySearchCriteria;
import ita.entity.*;
import ita.enumeration.CampaignDetailStatus;
import ita.exception.CustomException;
import ita.exception.NotFoundException;
import ita.repository.CampaignDetailRepository;
import ita.specification.CampaignDetailSpecification;
import ita.util.EmailUtil;
import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static ita.enumeration.CampaignDetailStatus.SENT;
import static ita.enumeration.CampaignDetailStatus.SOFT_BOUNCED;
import static ita.enumeration.EntityType.CAMPAIGN_DETAIL_TYPE;

@Service
@Slf4j
public class CampaignDetailService {

    private final EmailServerConfig emailServer;
    private final CampaignDetailRepository campaignDetailRepository;

    @Autowired
    @Qualifier("smtpSession")
    private Session smtpSession;

    private Transport transport;

    @Value("${tracker.url}")
    private String trackerUrl;

    @Value("${unsubscribe.url}")
    private String unsubscribeUrl;

    @Value("${param.header}")
    private final String[] paramHeaders;

    @Value("${param.seeding}")
    private final String[] paramSeeding;

    public CampaignDetailService(EmailServerConfig emailServer, CampaignDetailRepository campaignDetailRepository, String[] paramHeaders, String[] paramSeeding) {
        this.emailServer = emailServer;
        this.campaignDetailRepository = campaignDetailRepository;
        this.paramHeaders = paramHeaders;
        this.paramSeeding = paramSeeding;
    }

    public Page<CampaignDetailResponseDto> findAllCampaignDetail(CampaignHistorySearchCriteria searchCriteria) {
        Pageable pageable;

        if (searchCriteria.getType().equals("desc")) {
            pageable = PageRequest.of(searchCriteria.getPage(), searchCriteria.getSize());
        } else {
            pageable = PageRequest.of(searchCriteria.getPage(), searchCriteria.getSize());
        }

        UUID paramId = null;

        if (!searchCriteria.getId().isBlank()) {
            paramId = UUID.fromString(searchCriteria.getId());
        }

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
        try {
            processEmailCommunication(campaignHeader);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processEmailCommunication(CampaignHeader campaignHeader) throws MessagingException, IOException {
        ContactGroup contactGroup = campaignHeader.getContactGroup();

        List<Contact> contacts = contactGroup.getContacts();

        for (Contact contact: contacts) {
            if (!contact.getIsUnsubscribed()) {
                executeSendEmail(contact, campaignHeader);
            }
        }
    }

    private void executeSendEmail(Contact contact, CampaignHeader campaignHeader) {
        String email = contact.getEmail();

        if (!EmailUtil.isEmailAddressValid(email)) {
            throw new CustomException("Email address is not acceptable");
        }

        String trackerId = email.split("@")[0] + UUID.randomUUID();

        CampaignDetail campaignDetail = CampaignDetail.builder()
                .campaignHeader(campaignHeader)
                .contact(contact)
                .trackerId(trackerId)
                .status(SOFT_BOUNCED)
                .softBouncedAt(System.currentTimeMillis())
                .build();

        try {
            MimeMessage message = generateMultiMessage(campaignHeader, email, trackerId);

            transport = getTransport();

            if (!transport.isConnected()) {
                transport.connect(emailServer.getHost(), null, null);
            }

            transport.sendMessage(message, message.getAllRecipients());

            campaignDetail.setStatus(SENT);
            campaignDetail.setSentAt(System.currentTimeMillis());
            campaignDetail.setSoftBouncedAt(0L);

            campaignDetailRepository.save(campaignDetail);
        } catch (MessagingException | IOException e) {
            campaignDetail.setStatus(SOFT_BOUNCED);
            campaignDetail.setSoftBouncedAt(System.currentTimeMillis());
            campaignDetail.setSentAt(0L);

            campaignDetailRepository.save(campaignDetail);
            throw new RuntimeException(e);
        }
    }

    public ByteArrayResource generateCampaignHistory(Long startDate, Long endDate) {
        List<CampaignDetail> campaignDetails = campaignDetailRepository.findAllByTimeRange(startDate, endDate);
        List<String> headers = Arrays.asList(paramHeaders);

        try (Workbook workbook = new XSSFWorkbook()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("Campaign Report");
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }

            for (int i = 0; i < campaignDetails.size(); i++) {
                Row row = sheet.createRow(i + 1);

                CampaignDetail campaignDetail = campaignDetails.get(i);
                Optional<Content> content = Optional.ofNullable(campaignDetail.getCampaignHeader().getContent());

                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.createCell(j);
                    if (j == 0) cell.setCellValue(i + 1);
                    else if (j == 1) cell.setCellValue(getFormattedDate(campaignDetail.getSentAt()));
                    else if (j == 2) cell.setCellValue(campaignDetail.getContact().getEmail());
                    else if (j == 3) cell.setCellValue(campaignDetail.getStatus().name());
                    else if (j == 4) cell.setCellValue(campaignDetail.getContact().getName());
                    else if (j == 5) cell.setCellValue(content.isPresent() ? content.get().getName() : "");
                    else if (j == 6) cell.setCellValue(campaignDetail.getTrackerId());
                    else if (j == 17) cell.setCellValue(campaignDetail.getContact().getId().toString());
                    else if (j == 18) cell.setCellValue(campaignDetail.getCampaignHeader().getName());
                    else if (j == 19) cell.setCellValue(campaignDetail.getCampaignHeader().getSender().getEmail());
                    else if (j == 20) cell.setCellValue(campaignDetail.getCampaignHeader().getSender().getName());
                    else cell.setCellValue("");
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);

            byte[] bytes = outputStream.toByteArray();

            return new ByteArrayResource(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String getFormattedDate(Long date) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        return format.format(date);
    }

    private MimeMessage generateMultiMessage(CampaignHeader campaignHeader, String email, String trackerId) throws MessagingException, IOException {
        MimeMessage message = new MimeMessage(smtpSession);

        Multipart multipart = new MimeMultipart();

        message.setFrom(new InternetAddress(campaignHeader.getSender().getEmail()));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject(campaignHeader.getSubject());

        MimeBodyPart htmlBodyPart = new MimeBodyPart();

        String formattedContent = getEmailBodyWithTracker(decodeBase64(campaignHeader.getContent().getHtml()), trackerId);

        htmlBodyPart.setContent(formattedContent, "text/html; charset=utf-8");

        multipart.addBodyPart(htmlBodyPart);

        if (campaignHeader.getAttachment() != null) {
            MimeBodyPart attachmentPart = new MimeBodyPart();

            ByteArrayDataSource dataSource = new ByteArrayDataSource(campaignHeader.getAttachment().getFile(), "application/pdf");

            attachmentPart.setDataHandler(new DataHandler(dataSource));
            attachmentPart.setFileName(campaignHeader.getAttachment().getName());

            multipart.addBodyPart(attachmentPart);
        }

        message.setContent(multipart);

        return message;
    }

    private String getEmailBodyWithTracker(String body, String pixelId) {
        String imgUrl = trackerUrl + pixelId;

        String oldString = "href=\"\"";
        String newString = "href=\"" + unsubscribeUrl + pixelId + "\"";

        body = body.replace(oldString, newString);

        return String.format("<html><body>%1s<img src="+imgUrl+" width=\"1\" height=\"1\"></body>\n</html>", body);
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

    private Transport getTransport() throws NoSuchProviderException {
        if (this.transport == null) {
            this.transport = smtpSession.getTransport("smtp");
        }

        return this.transport;
    }

    private String decodeBase64(String base64String) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);

        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

}
