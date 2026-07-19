package com.acme.billing.repository;

import com.acme.billing.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findBySubscriptionId(Long subscriptionId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    long countBySubscriptionIdAndBillingPeriodStart(Long subscriptionId, java.time.LocalDate billingPeriodStart);
}
