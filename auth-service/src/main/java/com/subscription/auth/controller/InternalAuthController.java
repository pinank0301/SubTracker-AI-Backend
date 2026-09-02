package com.subscription.auth.controller;

import com.subscription.auth.dto.response.UserResponse;
import com.subscription.auth.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Internal controller for inter-service communication.
 * This endpoint is NOT exposed via the API Gateway —
 * only accessible to other microservices within the cluster.
 */
@Slf4j
@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
@Hidden  // Hide from Swagger UI
public class InternalAuthController {

    private final UserService userService;

    /**
     * Resolves a user's details (email, name) by their UUID userId.
     * Used internally by the notification-service via Feign to send emails.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        log.info("INTERNAL: Fetching user details for userId={}", userId);
        UserResponse userResponse = userService.getUserById(userId);
        return ResponseEntity.ok(userResponse);
    }
}
