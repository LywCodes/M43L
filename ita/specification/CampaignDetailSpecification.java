package ita.specification;

import ita.entity.CampaignDetail;
import ita.entity.CampaignHeader;
import ita.entity.Sender;
import ita.enumeration.CampaignDetailStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class CampaignDetailSpecification {
    private CampaignDetailSpecification (){}

    public static Specification<CampaignDetail> sentAtWithinDateRange(Long startDate, Long endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null || endDate == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.between(root.get("sentAt"), startDate, endDate);
        };
    }

    public static Specification<CampaignDetail> idLike(UUID id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    public static Specification<CampaignDetail> statusEqual(CampaignDetailStatus status) {
        return (root, query, criteriaBuilder) -> {
           if (status == null) {
               return criteriaBuilder.conjunction();
           }

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<CampaignDetail> nameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String lowerSearchTerm = "%" + name.toLowerCase() + "%";

            Join<CampaignDetail, CampaignHeader> parentJoin = root.join("campaignHeader");

            return criteriaBuilder.like(criteriaBuilder.lower(parentJoin.get("name")), lowerSearchTerm);
        };
    }

    public static Specification<CampaignDetail> senderEmailLike(String senderEmail) {
        return (root, query, criteriaBuilder) -> {
            if (senderEmail.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            Join<CampaignDetail, CampaignHeader> headerJoin = root.join("campaignHeader");

            Join<CampaignHeader, Sender> senderJoin = headerJoin.join("sender");

            String lowerSearchTerm = "%" + senderEmail.toLowerCase() + "%";

            return criteriaBuilder.like(criteriaBuilder.lower(senderJoin.get("email")), lowerSearchTerm);
        };
    }

}
