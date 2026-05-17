package ita.entity;

import ita.enumeration.ApprovalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseFullAuditEntity extends BaseAuditEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private Long approvedAt;

    @Column(name = "rejected_by", length = 100)
    private String rejectedBy;

    @Column(name = "rejected_at")
    private Long rejectedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    public void approve(String approver) {
        this.approvedBy = approver;
        this.approvedAt = Instant.now().toEpochMilli();
        this.approvalStatus = ApprovalStatus.APPROVED;
    }

    public void reject(String rejector, String reason) {
        this.rejectedBy = rejector;
        this.rejectedAt = Instant.now().toEpochMilli();
        this.rejectionReason = reason;
        this.approvalStatus = ApprovalStatus.REJECTED;
    }

    public boolean isPending() {
        return ApprovalStatus.PENDING.equals(this.approvalStatus);
    }

    public boolean isApproved() {
        return ApprovalStatus.APPROVED.equals(this.approvalStatus);
    }

}
