package com.acme.billing.repository;

import com.acme.billing.domain.Subscription;
import com.acme.billing.domain.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByCustomerId(Long customerId);

    @Query("select s from Subscription s " +
            "join fetch s.plan " +
            "join fetch s.customer " +
            "where s.status = :status and s.nextBillingDate <= :cutoff")
    List<Subscription> findDueForRenewal(SubscriptionStatus status, LocalDate cutoff);
}
