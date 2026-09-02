package com.subscription.subscription.constants;

public final class ApiConstants {

    private ApiConstants() {}

    public static final String BASE_PATH                = "/api/subscriptions";
    public static final String BY_CATEGORY_PATH         = "/category/{category}";
    public static final String UPCOMING_RENEWALS_PATH   = "/upcoming-renewals";
    public static final String BY_ID_PATH               = "/{id}";

    public static final String CREATED_SUCCESS          = "Subscription created successfully";
    public static final String RETRIEVED_SUCCESS        = "Subscription retrieved successfully";
    public static final String UPDATED_SUCCESS          = "Subscription updated successfully";
    public static final String CANCELLED_SUCCESS        = "Subscription cancelled successfully";
    public static final String LIST_SUCCESS             = "Subscriptions retrieved successfully";

    public static final String HEADER_USER_ID           = "X-User-Id";
    public static final String HEADER_USER_EMAIL        = "X-User-Email";

    public static final int    UPCOMING_RENEWAL_DAYS    = 7;
}
