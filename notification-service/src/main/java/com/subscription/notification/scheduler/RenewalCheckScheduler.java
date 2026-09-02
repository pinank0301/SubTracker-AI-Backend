package com.subscription.notification.scheduler;

import com.subscription.notification.service.NotificationProducer;
import com.subscription.notification.service.NotificationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class RenewalCheckScheduler {

    private final NotificationProducer  producer;
    private final NotificationProcessor processor;

    /**
     * Runs on a configurable cron schedule (default: every 6 hours).
     * Step 1: Produce — scan for upcoming renewals and queue notifications
     * Step 2: Process — send emails for queued notifications
     */
    @Scheduled(cron = "${notification.scheduler.cron}")
    public void checkAndNotifyRenewals() {
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  SCHEDULER: Renewal notification cycle started  ║");
        log.info("╚══════════════════════════════════════════════════╝");

        long startTime = System.currentTimeMillis();

        // Step 1: Produce
        int queued = producer.produceRenewalNotifications();

        // Step 2: Process
        int sent = processor.processNotifications();

        long duration = System.currentTimeMillis() - startTime;

        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  SCHEDULER: Cycle completed in {}ms          ", duration);
        log.info("║  Queued: {} | Sent: {}                         ", queued, sent);
        log.info("╚══════════════════════════════════════════════════╝");
    }
}
