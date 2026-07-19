package com.acme.billing.mapper;

import com.acme.billing.domain.Subscription;
import com.acme.billing.dto.SubscriptionDto;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public SubscriptionDto toDto(Subscription subscription) {
        return new SubscriptionDto(
                subscription.getId(),
                subscription.getCustomer().getId(),
                subscription.getCustomer().getFullName(),
                subscription.getPlan().getCode(),
                subscription.getStatus(),
                subscription.getStartDate(),
                subscription.getNextBillingDate()
        );
    }
}
