package ita.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApprovalStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    AUTO_CANCEL("Auto Cancelled"),
    CANCELLED("Cancelled");

    private final String value;
}
