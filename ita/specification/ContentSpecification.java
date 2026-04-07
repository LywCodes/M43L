package ita.specification;

import ita.entity.Content;
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

}
