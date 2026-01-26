package ita.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public enum JwtMessage {

    NULL_JWT_MESSAGE("JWT is null"),
    EXPIRED_JWT_MESSAGE("JWT is expired"),
    INVALID_JWT_MESSAGE("JWT is invalid");

    private final String value;
}
