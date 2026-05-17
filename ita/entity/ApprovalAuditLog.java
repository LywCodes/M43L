package ita.entity;

import ita.enumeration.ApprovalStatus;
import ita.enumeration.EntityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "approval_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "entity_identifier", nullable = false)
    private String entityIdentifier;

    @Column(name = "requester_email", nullable = false)
    private String requesterEmail;

    @Column(name = "approver_email")
    private String approverEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApprovalStatus status;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            this.createdAt = System.currentTimeMillis();
        }
    }
}
