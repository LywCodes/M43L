package ita.specification;

import ita.entity.Attachment;
import ita.entity.CampaignDetail;
import ita.entity.CampaignHeader;
import ita.enumeration.CampaignDetailStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static ita.enumeration.CampaignDetailStatus.*;

@Slf4j
public class StatisticSpecification {

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

            List<Predicate> predicates = new ArrayList<>();

            switch (status) {
                case SENT -> {
                    predicates.add(criteriaBuilder.equal(root.get("status"), SENT));
                    predicates.add(criteriaBuilder.between(root.get("sentAt"), startDate, endDate));
                }
                case OPENED -> {
                    predicates.add(criteriaBuilder.equal(root.get("status"), OPENED));
                    predicates.add(criteriaBuilder.between(root.get("openedAt"), startDate, endDate));
                }
                case CLICKED -> {
                    predicates.add(criteriaBuilder.equal(root.get("status"), CLICKED));
                    predicates.add(criteriaBuilder.between(root.get("clickedAt"), startDate, endDate));
                }
                case SOFT_BOUNCED -> {
                    predicates.add(criteriaBuilder.equal(root.get("status"), SOFT_BOUNCED));
                    predicates.add(criteriaBuilder.between(root.get("softBouncedAt"), startDate, endDate));
                }
                case HARD_BOUNCED -> {
                    predicates.add(criteriaBuilder.equal(root.get("status"), HARD_BOUNCED));
                    predicates.add(criteriaBuilder.between(root.get("hardBouncedAt"), startDate, endDate));
                }
                case UNSUBSCRIBED -> {
                    predicates.add(criteriaBuilder.equal(root.get("status"), UNSUBSCRIBED));
                    predicates.add(criteriaBuilder.between(root.get("unsubscribedAt"), startDate, endDate));
                }
                default -> criteriaBuilder.conjunction();
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[] {}));
        };
    }

}
