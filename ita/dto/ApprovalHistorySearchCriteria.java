package ita.dto;

import ita.enumeration.CampaignApprovalStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalHistorySearchCriteria extends BaseSearchCriteria{
    private String campaignName;
    private CampaignApprovalStatus status;
}
