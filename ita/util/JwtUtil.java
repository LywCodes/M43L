package ita.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import ita.dto.UserDetailsDto;
import ita.entity.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ita.enumeration.JwtStatus.*;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationInMs}")
    private int jwtExpirationInMs;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Value("${jwt.audience}")
    private String jwtAudience;

    public String generateJwt(UserDetailsImpl userDetails) {

        Set<String> permissions = userDetails.getPermissions().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        return Jwts.builder()
                .setId(String.valueOf(userDetails.getId()))
                .setSubject(userDetails.getUsername())
                .claim("permissions", permissions)
                .setIssuedAt(new Date())
                .setIssuer(jwtIssuer)
                .setAudience(jwtAudience)
                .setExpiration(new Date(new Date().getTime() + jwtExpirationInMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    public UserDetailsDto getJwtDetails(String token) {
        JwtParser parser = Jwts.parserBuilder().setSigningKey(key()).build();

        Claims claims = parser.parseClaimsJws(token).getBody();

        return UserDetailsDto.builder()
                .id(UUID.fromString(claims.getId()))
                .username(claims.getSubject())
                .permissions(getAuthorities((Collection<String>) claims.get("permissions")))
                .build();
    }

    private Set<SimpleGrantedAuthority> getAuthorities(Collection<String> permissions) {
        if (permissions.isEmpty()) {
            return Set.of();
        }

        return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    public String validateJwt(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return VALID_JWT.getValue();
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException exception) {
            return INVALID_JWT.getValue();
        } catch (ExpiredJwtException exception) {
            return EXPIRED_JWT.getValue();
        }
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

}
