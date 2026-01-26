package ita.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ita.enumeration.CampaignStatus;
import ita.enumeration.CampaignType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "campaign_header")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CampaignType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.SCHEDULED;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnore
    private Sender sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    @JsonIgnore
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_group_id", nullable = false)
    @JsonIgnore
    private ContactGroup contactGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_id")
    @JsonIgnore
    private Attachment attachment;

    @Column(name = "scheduled_time")
    private Long scheduledTime;

    @Column(name = "scheduled_at")
    private Long scheduledAt;

    @Column(name = "started_at")
    private Long startedAt;

    @Column(name = "completed_at")
    private Long completedAt;

    @Column(name = "failed_at")
    private Long failedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "quartz_job_id")
    private String quartzJobId;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }
}