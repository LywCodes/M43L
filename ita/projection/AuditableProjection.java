package ita.projection;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AuditableProjection {
    private final UUID id;
    private final String name;
    private final String approvalStatus;
    private final String createdBy;
    private final Long createdAt;
    private final String updatedBy;
    private final Long updatedAt;
    private final String approvedBy;
    private final Long approvedAt;
    private final String rejectedBy;
    private final Long rejectedAt;
    private final String rejectionReason;
}
