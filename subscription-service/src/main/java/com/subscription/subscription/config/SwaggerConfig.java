package com.subscription.subscription.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SUBSCRIPTION-SERVICE API")
                        .description("Subscription Management Microservice — manages user subscriptions. " +
                                "All requests must pass through API Gateway which validates JWT and " +
                                "injects X-User-Id and X-User-Email headers.")
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
                                .url("http://localhost:8082")
                                .description("Direct - Local Development (Port 8082)")
                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList("BearerAuth")
                        .addList("UserIdHeader")
                        .addList("UserEmailHeader")
                )
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Provide the Bearer JWT token when testing via the API Gateway (Port 8080).")
                        )
                        .addSecuritySchemes("UserIdHeader", new SecurityScheme()
                                .name("X-User-Id")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Provide UUID User ID when testing directly on Port 8082.")
                        )
                        .addSecuritySchemes("UserEmailHeader", new SecurityScheme()
                                .name("X-User-Email")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Provide User Email when testing directly on Port 8082.")
                        )
                );
    }
}
