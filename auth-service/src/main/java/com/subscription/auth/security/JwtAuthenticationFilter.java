package com.subscription.auth.security;

import com.subscription.auth.constants.ApiConstants;
import com.subscription.auth.exception.JwtValidationException;
import com.subscription.auth.service.CustomUserDetailsService;
import com.subscription.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(ApiConstants.AUTHORIZATION_HEADER);

        // Skip filter if no Bearer token present
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(ApiConstants.BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(ApiConstants.BEARER_PREFIX.length());
        log.debug("Processing JWT token for request: {} {}", request.getMethod(), request.getRequestURI());

        try {
            final String userEmail = jwtService.extractEmail(jwt);

            if (StringUtils.hasText(userEmail)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.validateToken(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("JWT authentication successful for user: {}", userEmail);
                } else {
                    log.warn("JWT validation failed for user: {}", userEmail);
                }
            }
        } catch (JwtValidationException ex) {
            log.warn("JWT validation error: {}", ex.getMessage());
            // Let the request continue; SecurityContext remains unauthenticated,
            // and the AuthenticationEntryPoint will handle 401
        }

        filterChain.doFilter(request, response);
    }
}
