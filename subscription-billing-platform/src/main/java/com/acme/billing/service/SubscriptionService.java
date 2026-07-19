package com.acme.billing.service;

import com.acme.billing.domain.Customer;
import com.acme.billing.domain.Plan;
import com.acme.billing.domain.Subscription;
import com.acme.billing.domain.SubscriptionStatus;
import com.acme.billing.dto.CreateSubscriptionRequest;
import com.acme.billing.dto.SubscriptionDto;
import com.acme.billing.exception.InvalidSubscriptionStateException;
import com.acme.billing.exception.ResourceNotFoundException;
import com.acme.billing.mapper.SubscriptionMapper;
import com.acme.billing.repository.CustomerRepository;
import com.acme.billing.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final PlanService planService;
    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                                CustomerRepository customerRepository,
                                PlanService planService,
                                SubscriptionMapper subscriptionMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.customerRepository = customerRepository;
        this.planService = planService;
        this.subscriptionMapper = subscriptionMapper;
    }

    @Transactional
    public SubscriptionDto createSubscription(CreateSubscriptionRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));
        Plan plan = planService.getPlanByCode(request.getPlanCode());

        Subscription subscription = new Subscription(customer, plan, LocalDate.now());
        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Created subscription {} for customer {} on plan {}", saved.getId(), customer.getId(), plan.getCode());
        return subscriptionMapper.toDto(saved);
    }

    @Transactional
    public SubscriptionDto cancelSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new InvalidSubscriptionStateException("Subscription is already cancelled: " + id);
        }
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(java.time.Instant.now());
        log.info("Cancelled subscription {}", id);
        return subscriptionMapper.toDto(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> listByCustomer(Long customerId) {
        return subscriptionRepository.findByCustomerId(customerId).stream()
                .map(subscriptionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionDto getSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));
        return subscriptionMapper.toDto(subscription);
    }
}
