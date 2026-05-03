package ita.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CampaignApprovalStatus {
    REQUEST_SUBMITTED("Submitted"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    AUTO_CANCEL("Cancelled"),
    REQUEST_CANCELLED("Expired");

    private final String value;
}
