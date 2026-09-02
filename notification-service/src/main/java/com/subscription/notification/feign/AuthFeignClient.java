package com.subscription.notification.feign;

import com.subscription.notification.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthFeignClient {

    @GetMapping("/internal/auth/users/{userId}")
    UserDto getUserById(@PathVariable("userId") UUID userId);
}
