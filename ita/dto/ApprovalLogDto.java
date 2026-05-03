package ita.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalLogDto {
    private String requesterName;
    private String approverName;
    private String campaignName;
    private String status;
    private String reason;
    private Long timestamp;
}
