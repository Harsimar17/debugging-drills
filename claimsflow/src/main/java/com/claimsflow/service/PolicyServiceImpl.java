package com.claimsflow.service;

import com.claimsflow.domain.Policy;
import com.claimsflow.dto.PolicyDto;
import com.claimsflow.exception.PolicyNotFoundException;
import com.claimsflow.mapper.PolicyMapper;
import com.claimsflow.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyMapper policyMapper;

    public PolicyServiceImpl(PolicyRepository policyRepository, PolicyMapper policyMapper) {
        this.policyRepository = policyRepository;
        this.policyMapper = policyMapper;
    }

    @Override
    public PolicyDto getByPolicyNumber(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new PolicyNotFoundException("No policy found with number " + policyNumber));
        return policyMapper.toDto(policy);
    }

    @Override
    public List<PolicyDto> getPoliciesForCustomer(Long customerId) {
        return policyRepository.findByCustomerId(customerId).stream()
                .map(policyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PolicyDto> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(policyMapper::toDto)
                .collect(Collectors.toList());
    }
}
