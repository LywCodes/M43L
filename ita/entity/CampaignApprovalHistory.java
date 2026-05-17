package ita.entity;

import ita.enumeration.CampaignApprovalStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Table(name = "campaign_approval_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "campaign_header_id", nullable = false)
    private CampaignHeader campaignHeader;

    @Column(name = "campaign_name", nullable = false)
    private String campaignName;

    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;

    @Column(name = "requester_name", nullable = false)
    private String requesterName;

    @Column(name = "approver_id", nullable = false)
    private UUID approverId;

    @Column(name = "approver_name", nullable = false)
    private String approverName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CampaignApprovalStatus status;

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
