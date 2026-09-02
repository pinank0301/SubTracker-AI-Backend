package com.subscription.subscription;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
public class SubscriptionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubscriptionServiceApplication.class, args);
        log.info("=============================================================");
        log.info("  SUBSCRIPTION-SERVICE started successfully on port 8082");
        log.info("  Swagger UI : http://localhost:8082/swagger-ui/index.html");
        log.info("  Actuator   : http://localhost:8082/actuator/health");
        log.info("=============================================================");
    }
}
