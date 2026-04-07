package ita.specification;

import ita.entity.CampaignDetail;
import ita.entity.CampaignHeader;
import ita.enumeration.CampaignDetailStatus;
import jakarta.persistence.criteria.Join;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
public class StatisticSpecification {
    private StatisticSpecification() {}

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

    public static Specification<CampaignDetail> statusEqual(CampaignDetailStatus status, Long startDate, Long endDate) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.name().isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String dateField = switch (status){
                case SENT -> "sentAt";
                case OPENED -> "openedAt";
                case CLICKED ->  "clickedAt";
                case SOFT_BOUNCED ->  "softBouncedAt";
                case HARD_BOUNCED ->  "hardBouncedAt";
                case UNSUBSCRIBED ->  "unsubscribedAt";
                default -> throw new IllegalArgumentException("Unknown status " + status);
            };
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("status"), status),
                    criteriaBuilder.between(root.get(dateField), startDate, endDate)
            );
        };
    }

}
