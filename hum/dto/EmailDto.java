package ita.dto;

import ita.entity.CampaignDetail;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class EmailDto {

    private String mimeMessageData;
    private CampaignDetail campaignDetail;

}
