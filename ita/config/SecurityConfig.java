package ita.config;

import ita.filter.AuthFilter;
import ita.filter.CorsFilter;
import ita.property.ResponseProperty;
import ita.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.PathMatcher;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@Slf4j
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${hsts.maxAge}")
    private int hstsMaxAge;

    @Value("${eai.api.key.id}")
    private String secretId;

    private static final String ALGORITHM = "DESede";

    private final JwtUtil jwtUtil;
    private final ResponseProperty responseProperty;
    private final PathMatcher pathMatcher;

    public SecurityConfig(JwtUtil jwtUtil, ResponseProperty responseProperty, PathMatcher pathMatcher) {
        this.jwtUtil = jwtUtil;
        this.responseProperty = responseProperty;
        this.pathMatcher = pathMatcher;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        httpSecurity.addFilterBefore(corsFilter(), UsernamePasswordAuthenticationFilter.class);
        httpSecurity.addFilterBefore(authFilter(), UsernamePasswordAuthenticationFilter.class);
        httpSecurity.headers(httpSecurityHeadersConfigurer ->
                httpSecurityHeadersConfigurer.httpStrictTransportSecurity(hstsConfig ->
                        hstsConfig.includeSubDomains(true)
                                .maxAgeInSeconds(hstsMaxAge)
                )
        );

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthFilter authFilter() {
        return new AuthFilter(jwtUtil, pathMatcher, responseProperty);
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecretKey secretKey() {
        try {
            byte[] keyBytes = secretId.getBytes();

            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to initialize Triple DES key: " + e.getMessage(), e);
        }
    }

}
