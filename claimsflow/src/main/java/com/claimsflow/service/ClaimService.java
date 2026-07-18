package com.claimsflow.service;

import com.claimsflow.dto.ClaimRequestDto;
import com.claimsflow.dto.ClaimResponseDto;
import com.claimsflow.dto.ClaimStatusUpdateDto;
import java.util.List;

public interface ClaimService {

    ClaimResponseDto submitClaim(ClaimRequestDto request);

    ClaimResponseDto getClaim(String claimNumber);

    List<ClaimResponseDto> getClaimsForPolicy(String policyNumber);

    ClaimResponseDto updateStatus(String claimNumber, ClaimStatusUpdateDto update);

    List<ClaimResponseDto> getAllClaims();
}
