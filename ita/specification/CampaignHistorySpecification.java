package ita.specification;

import ita.entity.CampaignApprovalHistory;
import ita.enumeration.CampaignApprovalStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class CampaignHistorySpecification {


    public static Specification<CampaignApprovalHistory> belongsToRequester(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("requesterId"), userId);
    }

    public static Specification<CampaignApprovalHistory> campaignNameLike(String name) {
        return (root, query, cb) -> (name == null || name.isBlank())
                ? null : cb.like(cb.lower(root.get("campaignName")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<CampaignApprovalHistory> statusEqual(CampaignApprovalStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
}