package ita.specification;

import ita.entity.Attachment;
import ita.entity.Content;
import ita.projection.BaseProjection;
import org.springframework.data.jpa.domain.Specification;

public class AttachmentSpecification {

    public static Specification<Attachment> nameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String lowerSearchTerm = "%" + name.toLowerCase() + "%";

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), lowerSearchTerm);
        };
    }

}
