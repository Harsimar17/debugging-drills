package com.claimsflow.service;

import com.claimsflow.domain.Policy;
import com.claimsflow.domain.enums.PolicyType;
import com.claimsflow.dto.PolicyDto;
import com.claimsflow.exception.PolicyNotFoundException;
import com.claimsflow.mapper.PolicyMapper;
import com.claimsflow.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyMapper policyMapper;

    @InjectMocks
    private PolicyServiceImpl policyService;

    @Test
    void getByPolicyNumberReturnsMappedDto() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setPolicyNumber("POL-AUTO-1001");
        policy.setPolicyType(PolicyType.AUTO);
        policy.setCoverageAmount(new BigDecimal("25000.00"));
        policy.setPremiumAmount(new BigDecimal("1200.00"));
        policy.setStartDate(LocalDate.now().minusMonths(6));
        policy.setEndDate(LocalDate.now().plusMonths(6));

        PolicyDto dto = new PolicyDto();
        dto.setPolicyNumber("POL-AUTO-1001");

        when(policyRepository.findByPolicyNumber("POL-AUTO-1001")).thenReturn(Optional.of(policy));
        when(policyMapper.toDto(policy)).thenReturn(dto);

        PolicyDto result = policyService.getByPolicyNumber("POL-AUTO-1001");

        assertEquals("POL-AUTO-1001", result.getPolicyNumber());
    }

    @Test
    void getByPolicyNumberThrowsWhenMissing() {
        when(policyRepository.findByPolicyNumber("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(PolicyNotFoundException.class, () -> policyService.getByPolicyNumber("UNKNOWN"));
    }
}
