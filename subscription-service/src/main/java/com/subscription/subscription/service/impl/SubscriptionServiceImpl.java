package com.subscription.subscription.service.impl;

import com.subscription.subscription.constants.ApiConstants;
import com.subscription.subscription.dto.request.CreateSubscriptionRequest;
import com.subscription.subscription.dto.request.UpdateSubscriptionRequest;
import com.subscription.subscription.dto.response.SubscriptionResponse;
import com.subscription.subscription.entity.Subscription;
import com.subscription.subscription.entity.SubscriptionStatus;
import com.subscription.subscription.exception.SubscriptionNotFoundException;
import com.subscription.subscription.exception.UnauthorizedResourceAccessException;
import com.subscription.subscription.mapper.SubscriptionMapper;
import com.subscription.subscription.repository.SubscriptionRepository;
import com.subscription.subscription.service.SubscriptionService;
import com.subscription.subscription.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper     subscriptionMapper;

    // =========================================================
    //  Create
    // =========================================================

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        UUID userId = CurrentUserUtil.getCurrentUserId();
        log.info("Creating subscription for userId={} | name={}", userId, request.getName());

        Subscription subscription = subscriptionMapper.toEntity(request, userId);
        Subscription saved = subscriptionRepository.save(subscription);

        log.info("Subscription created: id={} userId={}", saved.getId(), userId);
        return subscriptionMapper.toResponse(saved);
    }

    // =========================================================
    //  Read All
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllSubscriptions() {
        UUID userId = CurrentUserUtil.getCurrentUserId();
        log.debug("Fetching all subscriptions for userId={}", userId);

        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
        log.info("Retrieved {} subscriptions for userId={}", subscriptions.size(), userId);
        return subscriptionMapper.toResponseList(subscriptions);
    }

    // =========================================================
    //  Read By Id
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(UUID id) {
        UUID userId = CurrentUserUtil.getCurrentUserId();
        log.debug("Fetching subscription id={} for userId={}", id, userId);

        Subscription subscription = subscriptionRepository.findByUserIdAndId(userId, id)
                .orElseThrow(() -> {
                    log.warn("Subscription id={} not found or not owned by userId={}", id, userId);
                    return new SubscriptionNotFoundException(id);
                });

        log.info("Subscription retrieved: id={} userId={}", id, userId);
        return subscriptionMapper.toResponse(subscription);
    }

    // =========================================================
    //  Update
    // =========================================================

    @Override
    @Transactional
    public SubscriptionResponse updateSubscription(UUID id, UpdateSubscriptionRequest request) {
        UUID userId = CurrentUserUtil.getCurrentUserId();
        log.info("Updating subscription id={} for userId={}", id, userId);

        Subscription subscription = subscriptionRepository.findByUserIdAndId(userId, id)
                .orElseThrow(() -> {
                    log.warn("Update failed — subscription id={} not found for userId={}", id, userId);
                    return new SubscriptionNotFoundException(id);
                });

        subscriptionMapper.updateEntityFromRequest(subscription, request);
        Subscription updated = subscriptionRepository.save(subscription);

        log.info("Subscription updated: id={} userId={}", id, userId);
        return subscriptionMapper.toResponse(updated);
    }

    // =========================================================
    //  Cancel (Soft Delete)
    // =========================================================

    @Override
    @Transactional
    public void cancelSubscription(UUID id) {
        UUID userId = CurrentUserUtil.getCurrentUserId();
        log.info("Cancelling subscription id={} for userId={}", id, userId);

        Subscription subscription = subscriptionRepository.findByUserIdAndId(userId, id)
                .orElseThrow(() -> {
                    log.warn("Cancel failed — subscription id={} not found for userId={}", id, userId);
                    return new SubscriptionNotFoundException(id);
                });

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);

        log.info("Subscription cancelled: id={} userId={}", id, userId);
    }

    // =========================================================
    //  Find By Category
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> findByCategory(String category) {
        UUID userId = CurrentUserUtil.getCurrentUserId();
        log.debug("Fetching subscriptions for userId={} | category={}", userId, category);

        List<Subscription> subscriptions =
                subscriptionRepository.findByUserIdAndCategory(userId, category);

        log.info("Found {} subscriptions for userId={} in category={}", subscriptions.size(), userId, category);
        return subscriptionMapper.toResponseList(subscriptions);
    }

    // =========================================================
    //  Upcoming Renewals (next 7 days)
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> upcomingRenewals() {
        UUID userId = CurrentUserUtil.getCurrentUserId();
        LocalDate today  = LocalDate.now();
        LocalDate cutoff = today.plusDays(ApiConstants.UPCOMING_RENEWAL_DAYS);

        log.debug("Fetching upcoming renewals for userId={} between {} and {}", userId, today, cutoff);

        List<Subscription> subscriptions = subscriptionRepository.findUpcomingRenewals(
                userId, SubscriptionStatus.ACTIVE, today, cutoff);

        log.info("Found {} upcoming renewals for userId={}", subscriptions.size(), userId);
        return subscriptionMapper.toResponseList(subscriptions);
    }
}
