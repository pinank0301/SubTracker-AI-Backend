package com.subscription.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ── AUTH SERVICE (Public) ──────────────────────────────
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://AUTH-SERVICE")
                )

                // ── SUBSCRIPTION SERVICE (Protected) ──────────────────
                .route("subscription-service", r -> r
                        .path("/api/subscriptions/**")
                        .uri("lb://SUBSCRIPTION-SERVICE")
                )

                // ── BILLING SERVICE (Protected) ────────────────────────
                .route("billing-service", r -> r
                        .path("/api/billing/**")
                        .uri("lb://BILLING-SERVICE")
                )

                // ── NOTIFICATION SERVICE (Protected) ──────────────────
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("lb://NOTIFICATION-SERVICE")
                )

                .build();
    }
}
