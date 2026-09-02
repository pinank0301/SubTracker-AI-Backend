package com.subscription.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.gateway.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UnauthorizedHandler implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        log.warn("Unauthorized access: method={} uri={} message={}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath(),
                ex.getMessage());

        return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                "Access denied - Please provide a valid Bearer token");
    }

    public Mono<Void> writeErrorResponse(ServerWebExchange exchange,
                                          HttpStatus status,
                                          String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Add manual CORS headers to prevent browser from raising CORS error on authentication failures
        String origin = exchange.getRequest().getHeaders().getFirst("Origin");
        exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", origin != null ? origin : "*");
        exchange.getResponse().getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        exchange.getResponse().getHeaders().add("Access-Control-Allow-Headers", "*");
        exchange.getResponse().getHeaders().add("Access-Control-Expose-Headers", "X-Correlation-Id, Authorization");

        ErrorResponse errorResponse = ErrorResponse.of(
                message,
                status.value(),
                exchange.getRequest().getURI().getPath()
        );

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            bytes = "{\"success\":false,\"message\":\"Error\"}".getBytes();
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
