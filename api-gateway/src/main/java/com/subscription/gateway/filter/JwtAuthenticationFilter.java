package com.subscription.gateway.filter;

import com.subscription.gateway.constants.SecurityConstants;
import com.subscription.gateway.security.JwtValidator;
import com.subscription.gateway.security.UnauthorizedHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtValidator        jwtValidator;
    private final UnauthorizedHandler unauthorizedHandler;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/**",
            "/health",
            "/actuator/health",
            "/actuator/info"
    );

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange,
                              @NonNull WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Skip JWT validation for public endpoints
        if (isPublicPath(path)) {
            log.debug("Public path — skipping JWT validation: {}", path);
            return chain.filter(exchange);
        }

        // Extract Authorization header
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(SecurityConstants.AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authHeader)
                || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            log.warn("Missing or malformed Authorization header for path: {}", path);
            return unauthorizedHandler.writeErrorResponse(
                    exchange, HttpStatus.UNAUTHORIZED, SecurityConstants.MSG_MISSING_TOKEN);
        }

        String token = authHeader.substring(SecurityConstants.BEARER_PREFIX_LENGTH);
        JwtValidator.ValidationResult result = jwtValidator.validate(token);

        if (!result.valid()) {
            log.warn("JWT validation failed for path={} | reason={}", path, result.errorMessage());
            return unauthorizedHandler.writeErrorResponse(
                    exchange, HttpStatus.UNAUTHORIZED, result.errorMessage());
        }

        log.debug("JWT validated — propagating userId={} email={} for path={}",
                result.userId(), result.email(), path);

        // Propagate user identity as headers downstream
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", result.userId())
                .header("X-User-Email", result.email())
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        // Set authentication in reactive security context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        result.email(), null, Collections.emptyList());

        return chain.filter(mutatedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }
}
