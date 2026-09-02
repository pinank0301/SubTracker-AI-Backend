package com.subscription.auth.util;

public final class JwtConstants {

    private JwtConstants() {}

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_EMAIL   = "email";
    public static final String TOKEN_TYPE    = "JWT";
    public static final String ALGORITHM     = "HS256";
}
