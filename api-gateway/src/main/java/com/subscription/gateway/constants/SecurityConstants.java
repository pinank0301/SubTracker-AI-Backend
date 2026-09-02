package com.subscription.gateway.constants;

public final class SecurityConstants {

    private SecurityConstants() {}

    // Header
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX         = "Bearer ";
    public static final int    BEARER_PREFIX_LENGTH  = BEARER_PREFIX.length();

    // Correlation
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    // Public endpoints
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/health",
            "/actuator/health",
            "/actuator/info"
    };

    // JWT claim keys — must match auth-service JwtConstants
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_EMAIL   = "email";

    // Error messages
    public static final String MSG_MISSING_TOKEN   = "Authorization token is missing";
    public static final String MSG_INVALID_TOKEN   = "Invalid or malformed token";
    public static final String MSG_EXPIRED_TOKEN   = "Token has expired";
    public static final String MSG_ACCESS_DENIED   = "Access denied";
    public static final String MSG_INTERNAL_ERROR  = "An unexpected error occurred";
}
