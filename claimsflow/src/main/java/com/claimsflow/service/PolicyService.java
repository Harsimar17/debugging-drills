package com.claimsflow.service;

import com.claimsflow.dto.PolicyDto;
import java.util.List;

public interface PolicyService {

    PolicyDto getByPolicyNumber(String policyNumber);

    List<PolicyDto> getPoliciesForCustomer(Long customerId);

    List<PolicyDto> getAllPolicies();
}
