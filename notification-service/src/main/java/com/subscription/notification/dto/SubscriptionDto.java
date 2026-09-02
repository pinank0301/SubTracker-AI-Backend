package com.subscription.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {

    private UUID id;
    private UUID userId;
    private String name;
    private String category;
    private BigDecimal amount;
    private String currency;
    private String billingCycle;
    private LocalDate renewalDate;
    private String status;
    private String description;
}
