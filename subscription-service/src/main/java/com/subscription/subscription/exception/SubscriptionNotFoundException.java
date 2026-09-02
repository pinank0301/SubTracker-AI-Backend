package com.subscription.subscription.exception;

import java.util.UUID;

public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException(UUID id) {
        super("Subscription not found with id: " + id);
    }

    public SubscriptionNotFoundException(String message) {
        super(message);
    }
}
