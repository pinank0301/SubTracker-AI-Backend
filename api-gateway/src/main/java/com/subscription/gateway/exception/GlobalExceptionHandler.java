package com.subscription.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.gateway.constants.SecurityConstants;
import com.subscription.gateway.dto.ErrorResponse;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-1)  // Must run before DefaultErrorWebExceptionHandler (order = -2 in Boot)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange,
                              @NonNull Throwable ex) {

        HttpStatus status;
        String message;

        if (ex instanceof JwtException) {
            status  = HttpStatus.UNAUTHORIZED;
            message = SecurityConstants.MSG_INVALID_TOKEN;
            log.warn("JwtException: {}", ex.getMessage());

        } else if (ex instanceof AccessDeniedException) {
            status  = HttpStatus.FORBIDDEN;
            message = SecurityConstants.MSG_ACCESS_DENIED;
            log.warn("AccessDeniedException: {}", ex.getMessage());

        } else if (ex instanceof AuthenticationException) {
            status  = HttpStatus.UNAUTHORIZED;
            message = SecurityConstants.MSG_INVALID_TOKEN;
            log.warn("AuthenticationException: {}", ex.getMessage());

        } else if (ex instanceof IllegalArgumentException) {
            status  = HttpStatus.BAD_REQUEST;
            message = ex.getMessage();
            log.warn("IllegalArgumentException: {}", ex.getMessage());

        } else if (ex instanceof ResponseStatusException rse) {
            status  = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null ? rse.getReason() : ex.getMessage();
            log.warn("ResponseStatusException: {} {}", status, message);

        } else {
            status  = HttpStatus.INTERNAL_SERVER_ERROR;
            message = SecurityConstants.MSG_INTERNAL_ERROR;
            log.error("Unhandled exception: {}", ex.getMessage(), ex);
        }

        return writeResponse(exchange, status, message);
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange,
                                     HttpStatus status,
                                     String message) {

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Add manual CORS headers to prevent browser from raising CORS error during filter failures
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
            bytes = "{\"success\":false,\"message\":\"Error processing response\"}".getBytes();
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
