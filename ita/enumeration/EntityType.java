package ita.enumeration;

import lombok.*;

@ToString
@Getter
@AllArgsConstructor
public enum EntityType {

    AUTH_TYPE("Auth"),
    ROLE_TYPE("Role"),
    USER_TYPE("User"),
    CONTACT_TYPE("Contact"),
    CONTACT_GROUP_TYPE("Contact Group"),
    SENDER_TYPE("Sender"),
    CONTENT_TYPE("Content"),
    CAMPAIGN_DETAIL_TYPE("Campaign Detail"),
    CAMPAIGN_HEADER_TYPE("Campaign Header"),
    UNSUBSCRIBE_TYPE("Unsubscribe"),
    STATISTIC_TYPE("Statistic"),
    ATTACHMENT_TYPE("Attachment"),
    PERMISSION_TYPE("Permission"),
    AUDIT_TYPE("Audit"),
    ERROR_TYPE("Error"),
    NULL_TYPE("Null");

    private final String value;

}
