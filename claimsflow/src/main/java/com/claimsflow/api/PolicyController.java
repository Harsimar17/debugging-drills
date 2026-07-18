package com.claimsflow.api;

import com.claimsflow.dto.PolicyDto;
import com.claimsflow.service.PolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping("/{policyNumber}")
    public ResponseEntity<PolicyDto> getPolicy(@PathVariable String policyNumber) {
        return ResponseEntity.ok(policyService.getByPolicyNumber(policyNumber));
    }

    @GetMapping
    public ResponseEntity<List<PolicyDto>> getPolicies(
            @RequestParam(required = false) Long customerId) {
        if (customerId != null) {
            return ResponseEntity.ok(policyService.getPoliciesForCustomer(customerId));
        }
        return ResponseEntity.ok(policyService.getAllPolicies());
    }
}
