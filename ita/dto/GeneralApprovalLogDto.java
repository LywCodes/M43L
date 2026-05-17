package ita.dto;

import ita.enumeration.ApprovalStatus;
import ita.enumeration.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralApprovalLogDto {
    private UUID entityId;
    private EntityType entityType;
    private String entityIdentifier;
    private ApprovalStatus status;
    private String requesterEmail;
    private String approverEmail;
    private String reason;
}
