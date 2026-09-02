package com.subscription.notification.service;

import com.subscription.notification.dto.SubscriptionDto;
import com.subscription.notification.entity.NotificationOutbox;
import com.subscription.notification.entity.NotificationStatus;
import com.subscription.notification.feign.SubscriptionFeignClient;
import com.subscription.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final SubscriptionFeignClient       subscriptionFeignClient;
    private final NotificationOutboxRepository  outboxRepository;

    @Value("${notification.retry.max-attempts}")
    private int maxRetries;

    /**
     * Fetches upcoming renewals from subscription-service and inserts
     * deduplicated PENDING rows into the notification_outbox table.
     *
     * @return number of new notifications queued
     */
    @Transactional
    public int produceRenewalNotifications() {
        log.info("=== PRODUCER: Fetching upcoming renewals from SUBSCRIPTION-SERVICE ===");

        List<SubscriptionDto> renewals;
        try {
            renewals = subscriptionFeignClient.getUpcomingRenewals();
        } catch (Exception e) {
            log.error("PRODUCER: Failed to fetch renewals from SUBSCRIPTION-SERVICE: {}", e.getMessage());
            return 0;
        }

        if (renewals == null || renewals.isEmpty()) {
            log.info("PRODUCER: No upcoming renewals found");
            return 0;
        }

        log.info("PRODUCER: Found {} upcoming renewals", renewals.size());

        int queued = 0;
        for (SubscriptionDto sub : renewals) {

            // Deduplication — skip if notification already exists for this subscription+renewalDate
            boolean alreadyExists = outboxRepository.existsBySubscriptionIdAndRenewalDate(
                    sub.getId(), sub.getRenewalDate());

            if (alreadyExists) {
                log.debug("PRODUCER: Skipping duplicate — notification already queued for " +
                          "subscriptionId={} renewalDate={}", sub.getId(), sub.getRenewalDate());
                continue;
            }

            NotificationOutbox notification = NotificationOutbox.builder()
                    .subscriptionId(sub.getId())
                    .userId(sub.getUserId())
                    .subscriptionName(sub.getName())
                    .renewalDate(sub.getRenewalDate())
                    .amount(sub.getAmount())
                    .currency(sub.getCurrency())
                    .status(NotificationStatus.PENDING)
                    .retryCount(0)
                    .maxRetries(maxRetries)
                    .build();

            outboxRepository.save(notification);
            queued++;

            log.info("PRODUCER: Queued notification for userId={} subscription='{}' renewalDate={}",
                    sub.getUserId(), sub.getName(), sub.getRenewalDate());
        }

        log.info("=== PRODUCER: Queued {} new notifications (skipped {} duplicates) ===",
                queued, renewals.size() - queued);

        return queued;
    }
}
