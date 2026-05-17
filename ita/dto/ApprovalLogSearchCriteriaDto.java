package ita.dto;

import ita.enumeration.ApprovalStatus;
import ita.enumeration.EntityType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApprovalLogSearchCriteriaDto extends BaseSearchCriteria{
    private EntityType entityType;
    private ApprovalStatus status;
    private String requesterEmail;
    private String approverEmail;
//    private String entityIdentifier;
}
