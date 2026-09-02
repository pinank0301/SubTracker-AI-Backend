package com.subscription.subscription.exception;

import com.subscription.subscription.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================
    //  Custom Application Exceptions
    // =========================================================

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionNotFoundException(
            SubscriptionNotFoundException ex, HttpServletRequest request) {
        log.error("SubscriptionNotFoundException: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedResourceAccessException(
            UnauthorizedResourceAccessException ex, HttpServletRequest request) {
        log.warn("UnauthorizedResourceAccessException: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // =========================================================
    //  Validation
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String msg   = error.getDefaultMessage();
            errors.put(field, msg);
        });
        log.warn("Validation failed for path={} | errors={}", request.getRequestURI(), errors);

        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .message("Validation failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("HttpMessageNotReadableException: {} | path={}", ex.getMessage(), request.getRequestURI());
        String message = "Malformed JSON request or invalid enum value (allowed billingCycle values: MONTHLY, QUARTERLY, HALF_YEARLY, YEARLY)";
        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    // =========================================================
    //  Generic
    // =========================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.error("IllegalArgumentException: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {
        log.error("IllegalStateException: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: {} | path={}", ex.getMessage(), request.getRequestURI(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", request);
    }

    // =========================================================
    //  Helper
    // =========================================================

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message,
                                                      HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .message(message)
                .status(status.value())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
