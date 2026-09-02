package com.subscription.notification.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Slf4j
public final class CurrentUserUtil {

    private CurrentUserUtil() {}

    private static final String HEADER_USER_ID    = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";

    public static UUID getUserId() {
        String header = getHeader(HEADER_USER_ID);
        if (header == null || header.isBlank()) {
            throw new IllegalArgumentException(
                    "X-User-Id header is missing — request did not pass through API Gateway");
        }
        try {
            return UUID.fromString(header);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid X-User-Id header format: " + header);
        }
    }

    public static String getUserEmail() {
        String header = getHeader(HEADER_USER_EMAIL);
        if (header == null || header.isBlank()) {
            throw new IllegalArgumentException(
                    "X-User-Email header is missing — request did not pass through API Gateway");
        }
        return header;
    }

    private static String getHeader(String name) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest request = attrs.getRequest();
        return request.getHeader(name);
    }
}
