package com.acme.billing.service;

import com.acme.billing.domain.*;
import com.acme.billing.dto.CreateSubscriptionRequest;
import com.acme.billing.exception.InvalidSubscriptionStateException;
import com.acme.billing.mapper.SubscriptionMapper;
import com.acme.billing.repository.CustomerRepository;
import com.acme.billing.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PlanService planService;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscriptionService = new SubscriptionService(
                subscriptionRepository, customerRepository, planService, new SubscriptionMapper());
    }

    @Test
    void createsSubscriptionForExistingCustomerAndPlan() {
        Customer customer = new Customer("Jane Doe", "jane@example.com", "1 Main St");
        Plan plan = new Plan("PRO_MONTHLY", "Pro Monthly", new BigDecimal("29.99"), BillingCycle.MONTHLY);

        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setCustomerId(1L);
        request.setPlanCode("PRO_MONTHLY");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(planService.getPlanByCode("PRO_MONTHLY")).thenReturn(plan);
        when(subscriptionRepository.save(org.mockito.ArgumentMatchers.any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var dto = subscriptionService.createSubscription(request);

        assertThat(dto.getPlanCode()).isEqualTo("PRO_MONTHLY");
        assertThat(dto.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void cannotCancelAlreadyCancelledSubscription() {
        Customer customer = new Customer("Jane Doe", "jane@example.com", "1 Main St");
        Plan plan = new Plan("PRO_MONTHLY", "Pro Monthly", new BigDecimal("29.99"), BillingCycle.MONTHLY);
        Subscription subscription = new Subscription(customer, plan, LocalDate.now());
        subscription.setStatus(SubscriptionStatus.CANCELLED);

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> subscriptionService.cancelSubscription(1L))
                .isInstanceOf(InvalidSubscriptionStateException.class);
    }
}
