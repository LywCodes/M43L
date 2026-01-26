package ita.util;

import io.jsonwebtoken.MalformedJwtException;
import ita.entity.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

import static ita.enumeration.JwtMessage.EXPIRED_JWT_MESSAGE;
import static ita.enumeration.JwtMessage.NULL_JWT_MESSAGE;

@Slf4j
public class AuthUtil {

    public static UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getId();
        } else {
            Optional<String> authHeader = Optional.ofNullable(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization"));
            if (authHeader.isEmpty()) {
                throw new MalformedJwtException(NULL_JWT_MESSAGE.getValue());
            }

            throw new AuthenticationCredentialsNotFoundException(EXPIRED_JWT_MESSAGE.getValue());
        }
    }

    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getUsername();
        } else {
            Optional<String> authHeader = Optional.ofNullable(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization"));
            if (authHeader.isEmpty()) {
                throw new MalformedJwtException(NULL_JWT_MESSAGE.getValue());
            }

            throw new AuthenticationCredentialsNotFoundException(EXPIRED_JWT_MESSAGE.getValue());
        }
    }

}
