package ita.specification;

import ita.entity.Permission;
import org.springframework.data.jpa.domain.Specification;

public class PermissionSpecification {
    private PermissionSpecification(){}

    public static Specification<Permission> nameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String lowerSearchTerm = "%" + name.toLowerCase() + "%";

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), lowerSearchTerm);
        };
    }

}
