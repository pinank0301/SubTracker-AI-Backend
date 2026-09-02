package com.subscription.notification.constants;

public final class ApiConstants {

    private ApiConstants() {}

    public static final String BASE_PATH              = "/api/notifications";
    public static final String PENDING_PATH           = "/pending";
    public static final String FAILED_PATH            = "/failed";
    public static final String TRIGGER_PATH           = "/trigger";

    public static final String TRIGGER_SUCCESS        = "Notification cycle triggered successfully";
    public static final String PENDING_RETRIEVED      = "Pending notifications retrieved successfully";
    public static final String FAILED_RETRIEVED       = "Failed notifications retrieved successfully";
    public static final String FAILED_CLEARED         = "Failed notifications cleared successfully";
}
