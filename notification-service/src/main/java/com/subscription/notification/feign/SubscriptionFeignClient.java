package com.subscription.notification.feign;

import com.subscription.notification.dto.SubscriptionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "SUBSCRIPTION-SERVICE")
public interface SubscriptionFeignClient {

    @GetMapping("/internal/subscriptions/upcoming-renewals")
    List<SubscriptionDto> getUpcomingRenewals();
}
