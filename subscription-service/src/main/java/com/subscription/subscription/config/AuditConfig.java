package com.subscription.subscription.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class AuditConfig {
    // Enables @CreatedDate and @LastModifiedDate on Subscription entity
}
