package com.subscription.subscription.util;

import com.subscription.subscription.constants.ApiConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public final class CurrentUserUtil {

    private CurrentUserUtil() {}

    public static UUID getCurrentUserId() {
        String userIdHeader = getHeader(ApiConstants.HEADER_USER_ID);
        if (!StringUtils.hasText(userIdHeader)) {
            log.warn("X-User-Id header is missing from the request");
            throw new IllegalStateException("X-User-Id header is missing — request did not pass through API Gateway");
        }
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException ex) {
            log.error("X-User-Id header has invalid UUID format: {}", userIdHeader);
            throw new IllegalStateException("X-User-Id header contains an invalid UUID: " + userIdHeader);
        }
    }

    public static String getCurrentUserEmail() {
        String email = getHeader(ApiConstants.HEADER_USER_EMAIL);
        if (!StringUtils.hasText(email)) {
            log.warn("X-User-Email header is missing from the request");
            throw new IllegalStateException("X-User-Email header is missing — request did not pass through API Gateway");
        }
        return email;
    }

    public static Optional<UUID> getCurrentUserIdOptional() {
        try {
            return Optional.of(getCurrentUserId());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private static String getHeader(String headerName) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            log.warn("No request context found when reading header: {}", headerName);
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        return request.getHeader(headerName);
    }
}
