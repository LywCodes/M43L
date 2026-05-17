package ita.specification;

import ita.entity.ApprovalAuditLog;
import ita.enumeration.ApprovalStatus;
import ita.enumeration.EntityType;
import org.springframework.data.jpa.domain.Specification;

public class ApprovalLogSpecification {

    public static Specification<ApprovalAuditLog>entityTypeEqual(EntityType type) {
        return (root, query, cb) -> type == null
                ? cb.conjunction()
                : cb.equal(root.get("entity_type"), type);
    }

    public static Specification<ApprovalAuditLog>statusEqual(ApprovalStatus status) {
        return (root, query, cb) -> status == null
                ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    public static Specification<ApprovalAuditLog> requesterEmailLike(String email) {
        return (root, query, cb) -> (email == null || email.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("requesterEmail")), "%" + email.toLowerCase() + "%");
    }
    public static Specification<ApprovalAuditLog> approverEmailLike(String email) {
        return (root, query, cb) -> (email == null || email.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("approverEmail")), "%" + email.toLowerCase() + "%");
    }

    public static Specification<ApprovalAuditLog> entityIdentifierLike(String name) {
        return (root, query, cb) -> (name == null || name.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("entityIdentifier")), "%" + name.toLowerCase() + "%");
    }
}
