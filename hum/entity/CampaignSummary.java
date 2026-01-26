package ita.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "campaign_summary")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Integer day;
    private Integer month;
    private Integer year;
    private Long sentCount;
    private Long bouncedCount;
    private Long openedCount;
    private Long unsubscribedCount;

}
