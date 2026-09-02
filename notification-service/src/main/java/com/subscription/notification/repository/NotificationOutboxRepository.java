package com.subscription.notification.repository;

import com.subscription.notification.entity.NotificationOutbox;
import com.subscription.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {

    List<NotificationOutbox> findByStatus(NotificationStatus status);

    boolean existsBySubscriptionIdAndRenewalDate(UUID subscriptionId, LocalDate renewalDate);

    void deleteByStatus(NotificationStatus status);

    long countByStatus(NotificationStatus status);
}
