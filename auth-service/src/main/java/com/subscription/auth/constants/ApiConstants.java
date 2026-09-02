package com.subscription.auth.constants;

public final class ApiConstants {

    private ApiConstants() {}

    // Base paths
    public static final String AUTH_BASE_PATH   = "/api/auth";
    public static final String USERS_BASE_PATH  = "/api/users";

    // Auth endpoints
    public static final String REGISTER_ENDPOINT = "/register";
    public static final String LOGIN_ENDPOINT    = "/login";

    // User endpoints
    public static final String ME_ENDPOINT = "/me";

    // Messages
    public static final String USER_REGISTERED_SUCCESS  = "User Registered Successfully";
    public static final String LOGIN_SUCCESS             = "Login Successful";
    public static final String USER_FETCHED_SUCCESS      = "User fetched successfully";
    public static final String VALIDATION_FAILED         = "Validation failed";
    public static final String ACCESS_DENIED             = "Access denied";
    public static final String UNAUTHORIZED              = "Unauthorized - Please provide a valid Bearer token";
    public static final String INTERNAL_SERVER_ERROR     = "An unexpected error occurred";

    // Header
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX         = "Bearer ";

    // Swagger tags
    public static final String AUTH_TAG  = "Authentication";
    public static final String USERS_TAG = "Users";
}
