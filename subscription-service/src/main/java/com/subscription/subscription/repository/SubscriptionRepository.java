package com.subscription.subscription.repository;

import com.subscription.subscription.entity.Subscription;
import com.subscription.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByUserId(UUID userId);

    Optional<Subscription> findByUserIdAndId(UUID userId, UUID id);

    List<Subscription> findByUserIdAndCategory(UUID userId, String category);

    @Query("""
            SELECT s FROM Subscription s
            WHERE s.userId = :userId
              AND s.status = :status
              AND s.renewalDate BETWEEN :today AND :cutoff
            ORDER BY s.renewalDate ASC
            """)
    List<Subscription> findUpcomingRenewals(
            @Param("userId")  UUID userId,
            @Param("status")  SubscriptionStatus status,
            @Param("today")   LocalDate today,
            @Param("cutoff")  LocalDate cutoff
    );

    /**
     * Returns upcoming renewals for ALL users (no userId filter).
     * Used internally by the notification-service via Feign.
     */
    @Query("""
            SELECT s FROM Subscription s
            WHERE s.status = :status
              AND s.renewalDate BETWEEN :today AND :cutoff
            ORDER BY s.renewalDate ASC
            """)
    List<Subscription> findAllUpcomingRenewals(
            @Param("status")  SubscriptionStatus status,
            @Param("today")   LocalDate today,
            @Param("cutoff")  LocalDate cutoff
    );

    boolean existsByUserIdAndId(UUID userId, UUID id);
}
