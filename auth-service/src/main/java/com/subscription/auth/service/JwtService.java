package com.subscription.auth.service;

import com.subscription.auth.exception.JwtValidationException;
import com.subscription.auth.util.JwtConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // =========================================================
    //  Token Generation
    // =========================================================

    public String generateToken(UUID userId, String email) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(JwtConstants.CLAIM_USER_ID, userId.toString());
        extraClaims.put(JwtConstants.CLAIM_EMAIL, email);

        String token = buildToken(extraClaims, email, jwtExpiration);
        log.debug("Generated JWT token for user: {} | expiry: {}ms", email, jwtExpiration);
        return token;
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // =========================================================
    //  Claims Extraction
    // =========================================================

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(JwtConstants.CLAIM_USER_ID, String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token is expired: {}", ex.getMessage());
            throw new JwtValidationException("JWT token has expired");
        } catch (SignatureException ex) {
            log.warn("Invalid JWT signature: {}", ex.getMessage());
            throw new JwtValidationException("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.warn("Malformed JWT token: {}", ex.getMessage());
            throw new JwtValidationException("Malformed JWT token");
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
            throw new JwtValidationException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
            throw new JwtValidationException("JWT claims string is empty");
        }
    }

    // =========================================================
    //  Validation
    // =========================================================

    public boolean validateToken(String token, String email) {
        try {
            final String extractedEmail = extractEmail(token);
            boolean valid = extractedEmail.equals(email) && !isTokenExpired(token);
            if (valid) {
                log.debug("JWT token validated successfully for user: {}", email);
            } else {
                log.warn("JWT token validation failed for user: {}", email);
            }
            return valid;
        } catch (JwtValidationException ex) {
            log.warn("JWT validation exception for user {}: {}", email, ex.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // =========================================================
    //  Key Management
    // =========================================================

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
