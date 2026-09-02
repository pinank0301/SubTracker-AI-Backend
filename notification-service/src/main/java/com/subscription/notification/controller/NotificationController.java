package com.subscription.notification.controller;

import com.subscription.notification.constants.ApiConstants;
import com.subscription.notification.dto.ApiResponse;
import com.subscription.notification.entity.NotificationOutbox;
import com.subscription.notification.entity.NotificationStatus;
import com.subscription.notification.repository.NotificationOutboxRepository;
import com.subscription.notification.service.NotificationProducer;
import com.subscription.notification.service.NotificationProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(ApiConstants.BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management and manual trigger endpoints")
public class NotificationController {

    private final NotificationProducer         producer;
    private final NotificationProcessor        processor;
    private final NotificationOutboxRepository outboxRepository;

    // =========================================================
    //  POST /api/notifications/trigger — Manual trigger
    // =========================================================

    @Operation(summary = "Manually trigger notification cycle",
               description = "Runs the produce → process cycle immediately (for testing)")
    @PostMapping(ApiConstants.TRIGGER_PATH)
    public ResponseEntity<ApiResponse<Map<String, Integer>>> triggerCycle() {
        log.info("POST /api/notifications/trigger — manual trigger initiated");

        int queued = producer.produceRenewalNotifications();
        int sent   = processor.processNotifications();

        Map<String, Integer> result = Map.of(
                "queued", queued,
                "sent", sent
        );

        return ResponseEntity.ok(ApiResponse.success(ApiConstants.TRIGGER_SUCCESS, result));
    }

    // =========================================================
    //  GET /api/notifications/pending
    // =========================================================

    @Operation(summary = "View pending notifications",
               description = "Returns all notifications currently in PENDING status")
    @GetMapping(ApiConstants.PENDING_PATH)
    public ResponseEntity<ApiResponse<List<NotificationOutbox>>> getPending() {
        log.info("GET /api/notifications/pending");
        List<NotificationOutbox> pending = outboxRepository.findByStatus(NotificationStatus.PENDING);
        return ResponseEntity.ok(ApiResponse.success(ApiConstants.PENDING_RETRIEVED, pending));
    }

    // =========================================================
    //  GET /api/notifications/failed
    // =========================================================

    @Operation(summary = "View failed notifications",
               description = "Returns all notifications that have exceeded max retries")
    @GetMapping(ApiConstants.FAILED_PATH)
    public ResponseEntity<ApiResponse<List<NotificationOutbox>>> getFailed() {
        log.info("GET /api/notifications/failed");
        List<NotificationOutbox> failed = outboxRepository.findByStatus(NotificationStatus.FAILED);
        return ResponseEntity.ok(ApiResponse.success(ApiConstants.FAILED_RETRIEVED, failed));
    }

    // =========================================================
    //  DELETE /api/notifications/failed — Clear failed
    // =========================================================

    @Operation(summary = "Clear failed notifications",
               description = "Deletes all notifications with FAILED status")
    @DeleteMapping(ApiConstants.FAILED_PATH)
    @Transactional
    public ResponseEntity<ApiResponse<Void>> clearFailed() {
        log.info("DELETE /api/notifications/failed — clearing failed notifications");
        outboxRepository.deleteByStatus(NotificationStatus.FAILED);
        return ResponseEntity.ok(ApiResponse.success(ApiConstants.FAILED_CLEARED));
    }
}
