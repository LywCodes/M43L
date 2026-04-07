package ita.filter;

import com.google.gson.Gson;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import ita.dto.ResponseDto;
import ita.dto.UserDetailsDto;
import ita.entity.UserDetailsImpl;
import ita.property.ResponseProperty;
import ita.util.JwtUtil;
import ita.util.ResponseDtoUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Stream;

import static ita.enumeration.JwtMessage.*;
import static ita.enumeration.JwtStatus.*;
import static ita.util.ResponseDtoUtil.generatePayload;

@Slf4j
public class AuthFilter extends OncePerRequestFilter {

    @Value("${jwt.header}")
    private String jwtHeader;

    @Value("${jwt.prefix}")
    private String jwtPrefix;

    @Value("${cors.origin}")
    private String allowedOrigin;

    @Value("${cors.method}")
    private String allowedMethod;

    @Value("${cors.header}")
    private String allowedHeader;

    private final JwtUtil jwtUtil;
    private final PathMatcher pathMatcher;
    private final ResponseProperty responseProperty;

    private static final String[] allowedURI = {
            "/api/auth/login",
            "/api/campaign/detail/tracker/**",
            "/api/unsubscribe/tracker",
            "/api-docs/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/webjars/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    @Autowired
    public AuthFilter(JwtUtil jwtUtil, PathMatcher pathMatcher, ResponseProperty responseProperty) {
        this.jwtUtil = jwtUtil;
        this.pathMatcher = pathMatcher;
        this.responseProperty = responseProperty;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            String responseType;

            if (jwt == null) {
                handleNullJwt(response);

                return;
            }

            responseType = jwtUtil.validateJwt(jwt);

            if (!responseType.equals(VALID_JWT.getValue())) {
                handleInvalidJWT(response, responseType);

                return;
            }

            UserDetailsDto userDetailsDto = jwtUtil.getJwtDetails(jwt);

            UserDetailsImpl userDetails = new UserDetailsImpl(userDetailsDto.getId(), userDetailsDto.getUsername(), userDetailsDto.getPermissions());

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            userDetails.getName(),
                            userDetails.getAuthorities()
                    );

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (MalformedJwtException exception) {
            handleInvalidJWT(response, INVALID_JWT.getValue());

            return;
        } catch (ExpiredJwtException exception) {
            handleInvalidJWT(response, EXPIRED_JWT.getValue());

            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return Stream.of(allowedURI).anyMatch(url -> pathMatcher.match(url, request.getRequestURI()));
    }

    private String parseJwt(HttpServletRequest request) {
        String jwt = request.getHeader(jwtHeader);

        if (StringUtils.hasText(jwt) && jwt.startsWith(jwtPrefix)) {
            return jwt.substring(7);
        }

        return null;
    }

    private void handleNullJwt(HttpServletResponse response) throws IOException {
        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(responseProperty.getAccessDenied().getCode().getInvalid(),
                responseProperty.getAccessDenied().getMessage().getInvalid(), generatePayload(NULL_JWT_MESSAGE.getValue()));

        String responseJsonString = new Gson().toJson(responseDto);

        response.setStatus(403);
        response.setContentType("application/json");
        response.getWriter().write(responseJsonString);
    }

    private void handleInvalidJWT(HttpServletResponse response, String errorType) throws IOException {
        ResponseDto<Object> responseDto;

        if (errorType.equals(EXPIRED_JWT.getValue())) {
            responseDto = ResponseDtoUtil.generateResponse(responseProperty.getAccessDenied().getCode().getInvalid(),
                    responseProperty.getAccessDenied().getMessage().getInvalid(), generatePayload(EXPIRED_JWT_MESSAGE.getValue()));
        } else {
            responseDto = ResponseDtoUtil.generateResponse(responseProperty.getAccessDenied().getCode().getInvalid(),
                    responseProperty.getAccessDenied().getMessage().getInvalid(), generatePayload(INVALID_JWT_MESSAGE.getValue()));
        }

        String responseJsonString = new Gson().toJson(responseDto);

        response.setStatus(403);
        response.setContentType("application/json");
        response.getWriter().write(responseJsonString);
    }

}
