package com.subscription.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
        log.info("=============================================================");
        log.info("  AUTH-SERVICE started successfully");
        log.info("  Swagger UI: http://localhost:8081/swagger-ui/index.html");
        log.info("  API Docs  : http://localhost:8081/v3/api-docs");
        log.info("=============================================================");
    }
}
