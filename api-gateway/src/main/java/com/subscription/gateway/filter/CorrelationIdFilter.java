package com.subscription.gateway.filter;

import com.subscription.gateway.constants.SecurityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Dual-purpose filter:
 *  1. As a WebFilter (global) — attaches X-Correlation-Id to the response.
 *  2. As a GatewayFilterFactory (named "CorrelationIdFilter") — referenced in application.yml per-route.
 */
@Slf4j
@Component
@Order(1)
public class CorrelationIdFilter
        extends AbstractGatewayFilterFactory<CorrelationIdFilter.Config>
        implements WebFilter {

    public CorrelationIdFilter() {
        super(Config.class);
    }

    // =========================================================
    //  GatewayFilter (per-route, named in application.yml)
    // =========================================================

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = processCorrelationId(exchange);
            ServerWebExchange mutated = exchange.mutate().request(request).build();

            return chain.filter(mutated)
                    .then(Mono.fromRunnable(() -> addCorrelationIdToResponse(mutated)));
        };
    }

    // =========================================================
    //  WebFilter (global — also adds correlation id)
    // =========================================================

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange,
                              @NonNull WebFilterChain chain) {
        ServerHttpRequest request = processCorrelationId(exchange);
        ServerWebExchange mutated = exchange.mutate().request(request).build();

        return chain.filter(mutated)
                .doFinally(sig -> addCorrelationIdToResponse(mutated));
    }

    // =========================================================
    //  Helpers
    // =========================================================

    private ServerHttpRequest processCorrelationId(ServerWebExchange exchange) {
        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(SecurityConstants.CORRELATION_ID_HEADER);

        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated X-Correlation-Id: {}", correlationId);
        } else {
            log.debug("Reusing X-Correlation-Id: {}", correlationId);
        }

        final String finalCorrelationId = correlationId;
        return exchange.getRequest().mutate()
                .header(SecurityConstants.CORRELATION_ID_HEADER, finalCorrelationId)
                .build();
    }

    private void addCorrelationIdToResponse(ServerWebExchange exchange) {
        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(SecurityConstants.CORRELATION_ID_HEADER);

        ServerHttpResponse response = exchange.getResponse();
        if (!response.isCommitted() && StringUtils.hasText(correlationId)) {
            response.getHeaders().add(SecurityConstants.CORRELATION_ID_HEADER, correlationId);
        }
    }

    public static class Config {
        // No configuration properties needed
    }
}
