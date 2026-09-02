package com.subscription.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Slf4j
@SpringBootApplication
@EnableFeignClients
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("=============================================================");
        log.info("  NOTIFICATION-SERVICE started successfully on port 8083");
        log.info("  Swagger UI : http://localhost:8083/swagger-ui.html");
        log.info("  Actuator   : http://localhost:8083/actuator/health");
        log.info("=============================================================");
    }
}
