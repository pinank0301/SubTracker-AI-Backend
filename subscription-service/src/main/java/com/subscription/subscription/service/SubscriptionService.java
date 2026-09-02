package com.subscription.subscription.service;

import com.subscription.subscription.dto.request.CreateSubscriptionRequest;
import com.subscription.subscription.dto.request.UpdateSubscriptionRequest;
import com.subscription.subscription.dto.response.SubscriptionResponse;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {

    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);

    List<SubscriptionResponse> getAllSubscriptions();

    SubscriptionResponse getSubscriptionById(UUID id);

    SubscriptionResponse updateSubscription(UUID id, UpdateSubscriptionRequest request);

    void cancelSubscription(UUID id);

    List<SubscriptionResponse> findByCategory(String category);

    List<SubscriptionResponse> upcomingRenewals();
}
