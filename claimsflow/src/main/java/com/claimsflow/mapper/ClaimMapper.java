package com.claimsflow.mapper;

import com.claimsflow.domain.Adjuster;
import com.claimsflow.domain.Claim;
import com.claimsflow.dto.ClaimResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ClaimMapper {

    public ClaimResponseDto toDto(Claim claim) {
        ClaimResponseDto dto = new ClaimResponseDto();
        dto.setId(claim.getId());
        dto.setClaimNumber(claim.getClaimNumber());
        dto.setPolicyNumber(claim.getPolicy() != null ? claim.getPolicy().getPolicyNumber() : null);
        dto.setClaimType(claim.getClaimType());
        dto.setStatus(claim.getStatus());
        dto.setClaimedAmount(claim.getClaimedAmount());
        dto.setApprovedAmount(claim.getApprovedAmount());
        dto.setDescription(claim.getDescription());
        dto.setIncidentDate(claim.getIncidentDate());
        dto.setSubmittedAt(claim.getSubmittedAt());

        Adjuster adjuster = claim.getAssignedAdjuster();
        dto.setAssignedAdjusterName(adjuster != null ? adjuster.getFullName() : null);

        return dto;
    }
}
