package com.subscription.subscription.mapper;

import com.subscription.subscription.dto.request.CreateSubscriptionRequest;
import com.subscription.subscription.dto.request.UpdateSubscriptionRequest;
import com.subscription.subscription.dto.response.SubscriptionResponse;
import com.subscription.subscription.entity.Subscription;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SubscriptionMapper {

    public Subscription toEntity(CreateSubscriptionRequest request, UUID userId) {
        return Subscription.builder()
                .userId(userId)
                .name(request.getName())
                .category(request.getCategory())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .billingCycle(request.getBillingCycle())
                .renewalDate(request.getRenewalDate())
                .description(request.getDescription())
                .build();
    }

    public SubscriptionResponse toResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUserId())
                .name(subscription.getName())
                .category(subscription.getCategory())
                .amount(subscription.getAmount())
                .currency(subscription.getCurrency())
                .billingCycle(subscription.getBillingCycle())
                .renewalDate(subscription.getRenewalDate())
                .status(subscription.getStatus())
                .description(subscription.getDescription())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }

    public List<SubscriptionResponse> toResponseList(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .map(this::toResponse)
                .toList();
    }

    public void updateEntityFromRequest(Subscription subscription, UpdateSubscriptionRequest request) {
        subscription.setName(request.getName());
        subscription.setCategory(request.getCategory());
        subscription.setAmount(request.getAmount());
        subscription.setCurrency(request.getCurrency());
        subscription.setBillingCycle(request.getBillingCycle());
        subscription.setRenewalDate(request.getRenewalDate());
        subscription.setDescription(request.getDescription());
    }
}
