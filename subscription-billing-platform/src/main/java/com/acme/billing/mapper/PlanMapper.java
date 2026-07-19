package com.acme.billing.mapper;

import com.acme.billing.domain.Plan;
import com.acme.billing.dto.PlanDto;
import org.springframework.stereotype.Component;

@Component
public class PlanMapper {

    public PlanDto toDto(Plan plan) {
        return new PlanDto(
                plan.getId(),
                plan.getCode(),
                plan.getName(),
                plan.getPrice(),
                plan.getBillingCycle(),
                plan.isActive()
        );
    }
}
