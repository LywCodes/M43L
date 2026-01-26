package ita.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public enum JwtStatus {

    VALID_JWT("Valid"),
    INVALID_JWT("Invalid"),
    EXPIRED_JWT("Expired");

    private final String value;
}
