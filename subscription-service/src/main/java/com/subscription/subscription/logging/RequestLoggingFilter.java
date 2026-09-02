package com.subscription.subscription.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        long   startTime = System.currentTimeMillis();
        String method    = request.getMethod();
        String uri       = request.getRequestURI();
        String query     = request.getQueryString();
        String fullUri   = query != null ? uri + "?" + query : uri;

        log.info(">>> Request  | {} {}", method, fullUri);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            int  statusCode    = response.getStatus();

            if (statusCode >= 500) {
                log.error("<<< Response | {} {} {} {}ms", method, fullUri, statusCode, executionTime);
            } else if (statusCode >= 400) {
                log.warn("<<< Response | {} {} {} {}ms", method, fullUri, statusCode, executionTime);
            } else {
                log.info("<<< Response | {} {} {} {}ms", method, fullUri, statusCode, executionTime);
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator/health")
                || uri.startsWith("/actuator/info")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs");
    }
}
