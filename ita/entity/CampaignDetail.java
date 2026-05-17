package ita.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ita.enumeration.CampaignDetailStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Builder
@Entity
@Table(name = "campaign_detail")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "campaign_header_id", nullable = false)
    private CampaignHeader campaignHeader;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Contact contact;

    @JsonIgnore
    private String trackerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CampaignDetailStatus status = CampaignDetailStatus.QUEUED;

    private long sentAt;
    private long softBouncedAt;
    private long hardBouncedAt;
    private long clickedAt;
    private long openedAt;
    private long unsubscribedAt;


}
