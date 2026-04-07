package ita.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CorsFilter extends OncePerRequestFilter {

    @Value("${cors.origin}")
    private String allowedOriginString;

    @Value("${cors.method}")
    private String allowedMethod;

    @Value("${cors.header}")
    private String allowedHeader;

    private Set<String> allowedOrigins;

    @Override
    protected void initFilterBean() throws ServletException {
        this.allowedOrigins = new HashSet<>(Arrays.asList(allowedOriginString.split(",")));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String origin = request.getHeader("Origin");

        if (origin != null && allowedOrigins.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", allowedMethod);
            response.setHeader("Access-Control-Allow-Headers", allowedHeader);
        }

        if (request.getMethod().equals(HttpMethod.OPTIONS.name())) return;

        filterChain.doFilter(request, response);
    }
}
