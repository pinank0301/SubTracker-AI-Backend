package com.subscription.notification.service;

import com.subscription.notification.dto.UserDto;
import com.subscription.notification.entity.NotificationOutbox;
import com.subscription.notification.entity.NotificationStatus;
import com.subscription.notification.feign.AuthFeignClient;
import com.subscription.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProcessor {

    private final NotificationOutboxRepository outboxRepository;
    private final AuthFeignClient              authFeignClient;
    private final EmailService                 emailService;

    /**
     * Processes all PENDING notifications:
     * 1. Marks as PROCESSING
     * 2. Resolves userId → email via auth-service Feign call
     * 3. Sends HTML email
     * 4. On success → deletes the row (acknowledge)
     * 5. On failure → increments retry count or marks FAILED
     *
     * @return number of notifications successfully processed
     */
    @Transactional
    public int processNotifications() {
        List<NotificationOutbox> pendingList = outboxRepository.findByStatus(NotificationStatus.PENDING);

        if (pendingList.isEmpty()) {
            log.info("=== PROCESSOR: No pending notifications to process ===");
            return 0;
        }

        log.info("=== PROCESSOR: Processing {} pending notifications ===", pendingList.size());

        int successCount = 0;

        for (NotificationOutbox notification : pendingList) {
            try {
                processOne(notification);
                successCount++;
            } catch (Exception e) {
                handleFailure(notification, e);
            }
        }

        log.info("=== PROCESSOR: Completed — {} succeeded, {} failed ===",
                successCount, pendingList.size() - successCount);

        return successCount;
    }

    // =========================================================
    //  Process a Single Notification
    // =========================================================

    private void processOne(NotificationOutbox notification) {
        log.info("PROCESSOR: Processing notification id={} for subscription='{}'",
                notification.getId(), notification.getSubscriptionName());

        // Mark as PROCESSING
        notification.setStatus(NotificationStatus.PROCESSING);
        outboxRepository.save(notification);

        // Resolve userId → email via Feign
        UserDto user;
        try {
            user = authFeignClient.getUserById(notification.getUserId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve user email for userId=" +
                    notification.getUserId() + ": " + e.getMessage(), e);
        }

        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("No email found for userId=" + notification.getUserId());
        }

        // Calculate days until renewal
        long daysUntilRenewal = ChronoUnit.DAYS.between(LocalDate.now(), notification.getRenewalDate());

        // Build recipient name
        String recipientName = buildRecipientName(user);

        // Send email
        emailService.sendRenewalReminder(
                user.getEmail(),
                recipientName,
                notification.getSubscriptionName(),
                notification.getAmount(),
                notification.getCurrency(),
                notification.getRenewalDate(),
                daysUntilRenewal
        );

        // Success → delete the row (acknowledge)
        outboxRepository.delete(notification);
        log.info("PROCESSOR: Successfully sent and deleted notification id={} for user={}",
                notification.getId(), user.getEmail());
    }

    // =========================================================
    //  Failure Handling (Retry / Dead Letter)
    // =========================================================

    private void handleFailure(NotificationOutbox notification, Exception e) {
        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setErrorMessage(truncate(e.getMessage(), 1000));

        if (notification.getRetryCount() >= notification.getMaxRetries()) {
            notification.setStatus(NotificationStatus.FAILED);
            log.error("PROCESSOR: Notification id={} exceeded max retries ({}). Marked as FAILED. Error: {}",
                    notification.getId(), notification.getMaxRetries(), e.getMessage());
        } else {
            notification.setStatus(NotificationStatus.PENDING);
            log.warn("PROCESSOR: Notification id={} failed (attempt {}/{}). Will retry. Error: {}",
                    notification.getId(), notification.getRetryCount(),
                    notification.getMaxRetries(), e.getMessage());
        }

        outboxRepository.save(notification);
    }

    // =========================================================
    //  Helpers
    // =========================================================

    private String buildRecipientName(UserDto user) {
        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            String name = user.getFirstName();
            if (user.getLastName() != null && !user.getLastName().isBlank()) {
                name += " " + user.getLastName();
            }
            return name;
        }
        return user.getEmail();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "Unknown error";
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
