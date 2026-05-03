package ita.dto;

import ita.enumeration.CampaignStatus;
import ita.enumeration.CampaignType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class CampaignHeaderResponseDto {

    private UUID id;
    private String name;
    private String subject;
    private CampaignType type;
    private CampaignStatus status;
    private UUID senderId;
    private UUID contentId;
    private UUID attachmentId;
    private UUID contactGroupId;
    private Long scheduledTime;
    private UUID requesterId;
    private UUID approverId;
    private String rejectionReason;
    private Long createdAt;
}
