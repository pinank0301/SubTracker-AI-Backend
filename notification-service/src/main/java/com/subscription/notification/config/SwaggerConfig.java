package com.subscription.notification.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NOTIFICATION-SERVICE API")
                        .description("Notification Microservice — sends email reminders for upcoming " +
                                "subscription renewals using a database-backed outbox pattern (mimicking RabbitMQ).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Subscription Platform Team")
                                .email("team@subscription.com")
                        )
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Via API Gateway (Port 8080)"),
                        new Server()
                                .url("http://localhost:8083")
                                .description("Direct - Local Development (Port 8083)")
                ))
                .components(new Components());
    }
}
