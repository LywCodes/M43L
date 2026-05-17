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
public class CampaignApprovalResponseDto {

    private UUID id;
    private String name;
    private CampaignType type;
    private CampaignStatus status;
    private UUID senderId;
    private String senderName;
    private UUID contentId;
    private String contentName;
    private UUID attachmentId;
    private String attachmentName;
    private UUID contactGroupId;
    private String contactGroupName;
    private Long scheduledTime;
    private UUID requesterId;
    private String requesterName;
    private UUID approverId;
    private String approverName;
    private String rejectionReason;

}
