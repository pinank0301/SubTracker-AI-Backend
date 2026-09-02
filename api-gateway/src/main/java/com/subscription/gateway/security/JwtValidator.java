package com.subscription.gateway.security;

import com.subscription.gateway.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final JwtUtil jwtUtil;

    /**
     * Validates a raw token string (without "Bearer " prefix).
     *
     * @return ValidationResult with outcome details
     */
    public ValidationResult validate(String token) {
        if (!StringUtils.hasText(token)) {
            return ValidationResult.failure(SecurityConstants.MSG_MISSING_TOKEN);
        }

        try {
            Claims claims = jwtUtil.extractAllClaims(token);

            String email  = claims.getSubject();
            String userId = claims.get(SecurityConstants.CLAIM_USER_ID, String.class);

            if (!StringUtils.hasText(email) || !StringUtils.hasText(userId)) {
                log.warn("JWT is missing required claims: email={}, userId={}", email, userId);
                return ValidationResult.failure(SecurityConstants.MSG_INVALID_TOKEN);
            }

            if (jwtUtil.isTokenExpired(token)) {
                log.warn("JWT token has expired for user: {}", email);
                return ValidationResult.failure(SecurityConstants.MSG_EXPIRED_TOKEN);
            }

            log.debug("JWT validated successfully for user: {}", email);
            return ValidationResult.success(email, userId);

        } catch (ExpiredJwtException ex) {
            log.warn("JWT expired: {}", ex.getMessage());
            return ValidationResult.failure(SecurityConstants.MSG_EXPIRED_TOKEN);
        } catch (SignatureException ex) {
            log.warn("Invalid JWT signature: {}", ex.getMessage());
            return ValidationResult.failure(SecurityConstants.MSG_INVALID_TOKEN);
        } catch (MalformedJwtException ex) {
            log.warn("Malformed JWT token: {}", ex.getMessage());
            return ValidationResult.failure(SecurityConstants.MSG_INVALID_TOKEN);
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
            return ValidationResult.failure(SecurityConstants.MSG_INVALID_TOKEN);
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims empty: {}", ex.getMessage());
            return ValidationResult.failure(SecurityConstants.MSG_INVALID_TOKEN);
        }
    }

    // =========================================================
    //  Inner result type
    // =========================================================

    public record ValidationResult(boolean valid, String email, String userId, String errorMessage) {

        public static ValidationResult success(String email, String userId) {
            return new ValidationResult(true, email, userId, null);
        }

        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, null, null, errorMessage);
        }
    }
}
