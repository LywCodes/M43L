package ita.dto;

import ita.enumeration.ApprovalStatus;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
public abstract class AuditResponseDto {

    private ApprovalStatus approvalStatus;
    private String createdBy;
    private Long createdAt;
    private String updatedBy;
    private Long updatedAt;
    private String approvedBy;
    private Long approvedAt;
    private String rejectedBy;
    private Long rejectedAt;

}
