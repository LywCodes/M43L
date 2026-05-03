package ita.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public enum OperationType {
    ADD_OPERATION("Create"),
    READ_OPERATION("Read"),
    UPDATE_OPERATION("Update"),
    DELETE_OPERATION("Delete"),
    LOGIN_OPERATION("Login"),
    REGIST_OPERATION("Regist User"),
    DOWNLOAD_OPERATION("Download"),
    APPROVE_OPERATION("Approve"),
    REJECT_OPERATION("Reject");

    private final String value;
}
