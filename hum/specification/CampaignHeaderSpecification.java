package ita.specification;

import ita.entity.CampaignHeader;
import ita.entity.Contact;
import ita.enumeration.CampaignStatus;
import ita.enumeration.CampaignType;
import org.springframework.data.jpa.domain.Specification;

public class CampaignHeaderSpecification {

    public static Specification<CampaignHeader> statusEqual(CampaignStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<CampaignHeader> typeEqual(CampaignType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("type"), type);
        };
    }

    public static Specification<CampaignHeader> nameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String lowerSearchTerm = "%" + name.toLowerCase() + "%";

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), lowerSearchTerm);
        };
    }

    public static Specification<CampaignHeader> scheduledTime(Long scheduledTime) {
        return (root, query, criteriaBuilder) -> {
            if (scheduledTime == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(root.get("scheduledTime"), scheduledTime);
        };
    }

}
