package ita.dto;

import ita.enumeration.CampaignStatus;
import ita.enumeration.CampaignType;
import lombok.*;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CampaignHeaderSearchCriteria extends BaseSearchCriteria{

    private CampaignStatus status;
    private CampaignType campaignType;
    private Long scheduledTime;

}
