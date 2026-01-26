package ita.dto;

import ita.enumeration.CampaignDetailStatus;
import lombok.*;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class CampaignHistorySearchCriteria extends BaseSearchCriteria {

    private String id;
    private String senderEmail;
    private Long startDate;
    private Long endDate;
    private CampaignDetailStatus status;

}
