package ita.dto;

import ita.enumeration.CampaignStatus;
import ita.enumeration.CampaignType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApprovalSearchCriteria extends BaseSearchCriteria{
    private CampaignType campaignType;
    private CampaignStatus status;
}
