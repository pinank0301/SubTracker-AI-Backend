package com.subscription.subscription.controller;

import com.subscription.subscription.constants.ApiConstants;
import com.subscription.subscription.dto.request.CreateSubscriptionRequest;
import com.subscription.subscription.dto.request.UpdateSubscriptionRequest;
import com.subscription.subscription.dto.response.ApiResponse;
import com.subscription.subscription.dto.response.SubscriptionResponse;
import com.subscription.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(ApiConstants.BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription management endpoints")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // =========================================================
    //  POST /api/subscriptions
    // =========================================================

    @Operation(summary = "Create a new subscription",
               description = "Creates a subscription for the authenticated user")
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {

        log.info("POST /api/subscriptions — creating subscription: {}", request.getName());
        SubscriptionResponse response = subscriptionService.createSubscription(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApiConstants.CREATED_SUCCESS, response));
    }

    // =========================================================
    //  GET /api/subscriptions
    // =========================================================

    @Operation(summary = "Get all subscriptions",
               description = "Returns all subscriptions of the authenticated user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getAllSubscriptions() {

        log.info("GET /api/subscriptions — fetching all subscriptions");
        List<SubscriptionResponse> list = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(ApiResponse.success(ApiConstants.LIST_SUCCESS, list));
    }

    // =========================================================
    //  GET /api/subscriptions/{id}
    // =========================================================

    @Operation(summary = "Get subscription by ID",
               description = "Returns a specific subscription owned by the authenticated user")
    @GetMapping(ApiConstants.BY_ID_PATH)
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionById(
            @Parameter(description = "Subscription UUID") @PathVariable UUID id) {

        log.info("GET /api/subscriptions/{} — fetching subscription", id);
        SubscriptionResponse response = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(ApiResponse.success(ApiConstants.RETRIEVED_SUCCESS, response));
    }

    // =========================================================
    //  PUT /api/subscriptions/{id}
    // =========================================================

    @Operation(summary = "Update a subscription",
               description = "Updates an existing subscription owned by the authenticated user")
    @PutMapping(ApiConstants.BY_ID_PATH)
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
            @Parameter(description = "Subscription UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdateSubscriptionRequest request) {

        log.info("PUT /api/subscriptions/{} — updating subscription", id);
        SubscriptionResponse response = subscriptionService.updateSubscription(id, request);
        return ResponseEntity.ok(ApiResponse.success(ApiConstants.UPDATED_SUCCESS, response));
    }

    // =========================================================
    //  DELETE /api/subscriptions/{id}  (soft delete → CANCELLED)
    // =========================================================

    @Operation(summary = "Cancel a subscription",
               description = "Soft-deletes a subscription by setting its status to CANCELLED")
    @DeleteMapping(ApiConstants.BY_ID_PATH)
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @Parameter(description = "Subscription UUID") @PathVariable UUID id) {

        log.info("DELETE /api/subscriptions/{} — cancelling subscription", id);
        subscriptionService.cancelSubscription(id);
        return ResponseEntity.ok(ApiResponse.success(ApiConstants.CANCELLED_SUCCESS));
    }

    // =========================================================
    //  GET /api/subscriptions/category/{category}
    // =========================================================

    @Operation(summary = "Get subscriptions by category",
               description = "Returns all subscriptions of the authenticated user in the given category")
    @GetMapping(ApiConstants.BY_CATEGORY_PATH)
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> findByCategory(
            @Parameter(description = "Subscription category") @PathVariable String category) {

        log.info("GET /api/subscriptions/category/{} — fetching by category", category);
        List<SubscriptionResponse> list = subscriptionService.findByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(ApiConstants.LIST_SUCCESS, list));
    }

    // =========================================================
    //  GET /api/subscriptions/upcoming-renewals
    // =========================================================

    @Operation(summary = "Get upcoming renewals",
               description = "Returns ACTIVE subscriptions renewing within the next 7 days")
    @GetMapping(ApiConstants.UPCOMING_RENEWALS_PATH)
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> upcomingRenewals() {

        log.info("GET /api/subscriptions/upcoming-renewals — fetching upcoming renewals");
        List<SubscriptionResponse> list = subscriptionService.upcomingRenewals();
        return ResponseEntity.ok(ApiResponse.success(ApiConstants.LIST_SUCCESS, list));
    }
}
