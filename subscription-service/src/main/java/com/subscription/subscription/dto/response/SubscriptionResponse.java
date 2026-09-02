package com.subscription.subscription.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.subscription.subscription.entity.BillingCycle;
import com.subscription.subscription.entity.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Subscription details response")
public class SubscriptionResponse {

    @Schema(description = "Unique subscription identifier")
    private UUID id;

    @Schema(description = "Owner user ID")
    private UUID userId;

    @Schema(description = "Subscription name", example = "Netflix")
    private String name;

    @Schema(description = "Subscription category", example = "Entertainment")
    private String category;

    @Schema(description = "Subscription cost", example = "15.99")
    private BigDecimal amount;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Billing frequency")
    private BillingCycle billingCycle;

    @Schema(description = "Next renewal date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate renewalDate;

    @Schema(description = "Current status")
    private SubscriptionStatus status;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Creation timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
