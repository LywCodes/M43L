package ita.specification;

import ita.entity.Contact;
import org.springframework.data.jpa.domain.Specification;

public class ContactSpecification {

    public static Specification<Contact> nameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String lowerSearchTerm = "%" + name.toLowerCase() + "%";

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), lowerSearchTerm);
        };
    }

    public static Specification<Contact> emailLike(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String lowerSearchTerm = "%" + email.toLowerCase() + "%";

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), lowerSearchTerm);
        };
    }

}
