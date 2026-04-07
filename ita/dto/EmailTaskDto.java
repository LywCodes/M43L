package ita.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailTaskDto {
    private UUID campaignDetailId;
    private String senderEmail;
    private String recipientEmail;
    private String subject;
    private String htmlContent;
    private String attachmentName;
    private byte[] attachmentFile;
}