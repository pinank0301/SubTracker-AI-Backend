package com.subscription.subscription.controller;

import com.subscription.subscription.dto.response.SubscriptionResponse;
import com.subscription.subscription.entity.Subscription;
import com.subscription.subscription.entity.SubscriptionStatus;
import com.subscription.subscription.mapper.SubscriptionMapper;
import com.subscription.subscription.repository.SubscriptionRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Internal controller for inter-service communication.
 * This endpoint is NOT exposed via the API Gateway —
 * only accessible to other microservices within the cluster.
 */
@Slf4j
@RestController
@RequestMapping("/internal/subscriptions")
@RequiredArgsConstructor
@Hidden  // Hide from Swagger UI
public class InternalSubscriptionController {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper     subscriptionMapper;

    @Value("${notification.renewal.days-before:7}")
    private int renewalDaysBefore;

    /**
     * Returns all ACTIVE subscriptions across ALL users that have
     * a renewal date within the next N days (default: 7).
     * Used by the notification-service to queue renewal reminders.
     */
    @GetMapping("/upcoming-renewals")
    public ResponseEntity<List<SubscriptionResponse>> getAllUpcomingRenewals() {
        LocalDate today  = LocalDate.now();
        LocalDate cutoff = today.plusDays(renewalDaysBefore);

        log.info("INTERNAL: Fetching all upcoming renewals between {} and {}", today, cutoff);

        List<Subscription> subscriptions = subscriptionRepository.findAllUpcomingRenewals(
                SubscriptionStatus.ACTIVE, today, cutoff);

        log.info("INTERNAL: Found {} upcoming renewals across all users", subscriptions.size());

        List<SubscriptionResponse> responses = subscriptionMapper.toResponseList(subscriptions);
        return ResponseEntity.ok(responses);
    }
}
