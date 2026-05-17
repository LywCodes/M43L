package ita.specification;

import ita.entity.Content;
import ita.entity.Sender;
import ita.enumeration.ApprovalStatus;
import org.springframework.data.jpa.domain.Specification;

public class ContentSpecification {
    private ContentSpecification() {}

    public static Specification<Content> nameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String lowerSearchTerm = "%" + name.toLowerCase() + "%";

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), lowerSearchTerm);
        };
    }

    public static Specification<Content>hasStatus(ApprovalStatus status) {
        return ((root, query, criteriaBuilder) ->  status == null
                ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("approvalStatus"), status));
    }

}
