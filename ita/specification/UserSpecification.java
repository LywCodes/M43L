package ita.specification;

import ita.entity.LocalUser;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    private UserSpecification(){}

    public static Specification<LocalUser> nameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String lowerSearchTerm = "%" + name.toLowerCase() + "%";

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), lowerSearchTerm);
        };
    }

    public static Specification<LocalUser> usernameLike(String username) {
        return (root, query, criteriaBuilder) -> {
            if (username.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String lowerSearchTerm = "%" + username.toLowerCase() + "%";

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), lowerSearchTerm);
        };
    }

}
