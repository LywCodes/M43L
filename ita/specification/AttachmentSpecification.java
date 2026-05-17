package ita.specification;

import ita.entity.Attachment;
import ita.enumeration.ApprovalStatus;
import org.springframework.data.jpa.domain.Specification;

public class AttachmentSpecification {
    private AttachmentSpecification() {}

    public static Specification<Attachment> nameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String lowerSearchTerm = "%" + name.toLowerCase() + "%";

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), lowerSearchTerm);
        };
    }

    public static Specification<Attachment>hasStatus(ApprovalStatus status) {
        return ((root, query, criteriaBuilder) ->  status == null
                ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("approvalStatus"), status));
    }

}
