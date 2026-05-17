package ita.repository;

import ita.entity.ApprovalAuditLog;
import ita.enumeration.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ApprovalAuditLogRepository extends JpaRepository<ApprovalAuditLog, UUID>, JpaSpecificationExecutor<ApprovalAuditLog> {
    List<ApprovalAuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(EntityType entityType, UUID entityId);

    List<ApprovalAuditLog>findByRequesterEmailOrderByCreatedAtDesc(String requesterEmail);

    List<ApprovalAuditLog>findByApproverEmailOrderByCreatedAtDesc(String approverEmail);
}
