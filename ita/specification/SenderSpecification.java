package ita.specification;

import ita.entity.Sender;
import ita.enumeration.ApprovalStatus;
import ita.projection.AuditableProjection;
import org.springframework.data.jpa.domain.Specification;

public class SenderSpecification {
    private SenderSpecification() {}
    public static Specification<Sender> nameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String lowerSearchTerm = "%" + name.toLowerCase() + "%";

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), lowerSearchTerm);
        };
    }

    public static Specification<Sender> emailLike(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String lowerSearchTerm = "%" + email.toLowerCase() + "%";

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), lowerSearchTerm);
        };
    }

    public static Specification<Sender>hasStatus(ApprovalStatus status) {
        return ((root, query, criteriaBuilder) ->  status == null
                ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("approvalStatus"), status));
    }

}
