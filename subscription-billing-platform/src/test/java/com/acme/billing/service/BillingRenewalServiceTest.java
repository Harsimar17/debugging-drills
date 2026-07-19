package com.acme.billing.service;

import com.acme.billing.domain.*;
import com.acme.billing.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BillingRenewalServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private NotificationService notificationService;

    private BillingRenewalService billingRenewalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        billingRenewalService = new BillingRenewalService(subscriptionRepository, invoiceService, notificationService);
    }

    @Test
    void renewsAllSubscriptionsThatAreDue() {
        Customer customer = new Customer("Jane Doe", "jane@example.com", "1 Main St");
        Plan plan = new Plan("PRO_MONTHLY", "Pro Monthly", new BigDecimal("29.99"), BillingCycle.MONTHLY);
        Subscription subscriptionA = new Subscription(customer, plan, LocalDate.now());
        Subscription subscriptionB = new Subscription(customer, plan, LocalDate.now());

        when(subscriptionRepository.findDueForRenewal(any(), any()))
                .thenReturn(List.of(subscriptionA, subscriptionB));
        when(invoiceService.generateInvoiceForRenewal(any(Subscription.class)))
                .thenReturn(new Invoice("INV-TEST", subscriptionA, plan.getPrice(), LocalDate.now(), LocalDate.now().plusMonths(1)));

        var summary = billingRenewalService.runRenewalBatch();

        assertThat(summary.due()).isEqualTo(2);
        assertThat(summary.renewed()).isEqualTo(2);
        assertThat(summary.failed()).isZero();
    }

    @Test
    void skipsSubscriptionsWithNothingDue() {
        when(subscriptionRepository.findDueForRenewal(any(), any())).thenReturn(List.of());

        var summary = billingRenewalService.runRenewalBatch();

        assertThat(summary.due()).isZero();
        assertThat(summary.renewed()).isZero();
    }
}
