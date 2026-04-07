package ita.dto;

import ita.entity.CampaignDetail;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailBatchDto {
    private CampaignDetail campaignDetail;
    private EmailTaskDto emailTaskDto;
    private boolean invalidEmail;
}
