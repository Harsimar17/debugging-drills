package com.acme.billing.service;

import com.acme.billing.domain.*;
import com.acme.billing.mapper.InvoiceMapper;
import com.acme.billing.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        invoiceService = new InvoiceService(invoiceRepository, new InvoiceNumberGenerator(), new InvoiceMapper());
    }

    @Test
    void generatesInvoiceCoveringCurrentBillingPeriod() {
        Customer customer = new Customer("Jane Doe", "jane@example.com", "1 Main St");
        Plan plan = new Plan("PRO_MONTHLY", "Pro Monthly", new BigDecimal("29.99"), BillingCycle.MONTHLY);
        Subscription subscription = new Subscription(customer, plan, LocalDate.now());

        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice invoice = invoiceService.generateInvoiceForRenewal(subscription);

        assertThat(invoice.getAmount()).isEqualByComparingTo("29.99");
        assertThat(invoice.getBillingPeriodStart()).isEqualTo(subscription.getNextBillingDate());
        assertThat(invoice.getBillingPeriodEnd()).isEqualTo(subscription.getNextBillingDate().plusMonths(1));
        assertThat(invoice.getInvoiceNumber()).startsWith("INV-");
    }
}
