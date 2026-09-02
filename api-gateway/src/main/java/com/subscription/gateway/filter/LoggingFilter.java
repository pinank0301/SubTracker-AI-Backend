package com.subscription.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(2)
public class LoggingFilter implements WebFilter {

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange,
                              @NonNull WebFilterChain chain) {

        long startTime = System.currentTimeMillis();

        String method = exchange.getRequest().getMethod().name();
        String uri    = exchange.getRequest().getURI().getPath();

        log.info(">>> Request  | {} {}", method, uri);

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    ServerHttpResponse response = exchange.getResponse();
                    long executionTime = System.currentTimeMillis() - startTime;

                    int statusCode = response.getStatusCode() != null
                            ? response.getStatusCode().value()
                            : 0;

                    if (statusCode >= 500) {
                        log.error("<<< Response | {} {} {} {}ms",
                                method, uri, statusCode, executionTime);
                    } else if (statusCode >= 400) {
                        log.warn("<<< Response | {} {} {} {}ms",
                                method, uri, statusCode, executionTime);
                    } else {
                        log.info("<<< Response | {} {} {} {}ms",
                                method, uri, statusCode, executionTime);
                    }
                });
    }
}
