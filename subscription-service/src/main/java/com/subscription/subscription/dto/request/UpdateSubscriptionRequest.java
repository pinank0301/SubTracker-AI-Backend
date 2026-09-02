package com.subscription.subscription.dto.request;

import com.subscription.subscription.entity.BillingCycle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to update an existing subscription")
public class UpdateSubscriptionRequest {

    @NotBlank(message = "Subscription name is required")
    @Schema(description = "Name of the subscription", example = "Netflix Premium")
    private String name;

    @NotBlank(message = "Category is required")
    @Schema(description = "Category of the subscription", example = "Entertainment")
    private String category;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be a positive number")
    @Schema(description = "Subscription cost amount", example = "19.99")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Schema(description = "Currency code (ISO 4217)", example = "USD")
    private String currency;

    @NotNull(message = "Billing cycle is required")
    @Schema(description = "Billing frequency", example = "YEARLY")
    private BillingCycle billingCycle;

    @NotNull(message = "Renewal date is required")
    @Schema(description = "Next renewal date", example = "2027-09-01")
    private LocalDate renewalDate;

    @Schema(description = "Optional description", example = "Upgraded Netflix plan")
    private String description;
}
