package com.claimsflow.mapper;

import com.claimsflow.domain.Policy;
import com.claimsflow.dto.PolicyDto;
import org.springframework.stereotype.Component;

@Component
public class PolicyMapper {

    public PolicyDto toDto(Policy policy) {
        PolicyDto dto = new PolicyDto();
        dto.setId(policy.getId());
        dto.setPolicyNumber(policy.getPolicyNumber());
        dto.setPolicyType(policy.getPolicyType());
        dto.setCoverageAmount(policy.getCoverageAmount());
        dto.setPremiumAmount(policy.getPremiumAmount());
        dto.setStartDate(policy.getStartDate());
        dto.setEndDate(policy.getEndDate());
        dto.setActive(policy.isActive());
        dto.setCustomerId(policy.getCustomer() != null ? policy.getCustomer().getId() : null);
        return dto;
    }
}
