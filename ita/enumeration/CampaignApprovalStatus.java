package ita.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CampaignApprovalStatus {
    REQUEST_SUBMITTED("Submitted"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    AUTO_CANCEL("Expired"),
    REQUEST_CANCELLED("Cancelled");

    private final String value;
}
