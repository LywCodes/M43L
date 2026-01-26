package ita.specification;

import ita.entity.LocalRole;
import org.springframework.data.jpa.domain.Specification;

public class RoleSpecification {

    public static Specification<LocalRole> nameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String lowerSearchTerm = "%" + name.toLowerCase() + "%";

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), lowerSearchTerm);
        };
    }

}
