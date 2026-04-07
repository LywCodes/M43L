package ita.util;

import io.jsonwebtoken.MalformedJwtException;
import ita.entity.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static ita.enumeration.JwtMessage.EXPIRED_JWT_MESSAGE;
import static ita.enumeration.JwtMessage.NULL_JWT_MESSAGE;

@Slf4j
public final class AuthUtil {
    private AuthUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static UUID getUserId() {
        return getUserDetails().getId();
    }

    public static String getUsername() {
        return getUserDetails().getUsername();
    }


    public static UserDetailsImpl getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null &&!(authentication instanceof AnonymousAuthenticationToken)) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }

        validateAuthorizationHeader();
        throw  new AuthenticationCredentialsNotFoundException(EXPIRED_JWT_MESSAGE.getValue());
    }

    private static void validateAuthorizationHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new MalformedJwtException(NULL_JWT_MESSAGE.getValue());
        }

        String header = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || header.trim().isEmpty()) {
            throw new MalformedJwtException(NULL_JWT_MESSAGE.getValue());
        }
    }
}
