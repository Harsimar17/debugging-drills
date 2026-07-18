package com.claimsflow.service;

import com.claimsflow.domain.Claim;
import com.claimsflow.domain.Policy;
import com.claimsflow.domain.enums.ClaimType;
import com.claimsflow.dto.ClaimRequestDto;
import com.claimsflow.dto.ClaimResponseDto;
import com.claimsflow.mapper.ClaimMapper;
import com.claimsflow.repository.ClaimRepository;
import com.claimsflow.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ClaimMapper claimMapper;

    @Mock
    private ClaimNumberGeneratorService claimNumberGeneratorService;

    @Mock
    private AdjusterAssignmentService adjusterAssignmentService;

    @Mock
    private ClaimStatusService claimStatusService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private Policy policy;

    @BeforeEach
    void setUp() {
        policy = new Policy();
        policy.setId(1L);
        policy.setPolicyNumber("POL-AUTO-1001");
        policy.setActive(true);
    }

    @Test
    void submitClaimAssignsGeneratedClaimNumber() {
        ClaimRequestDto request = new ClaimRequestDto();
        request.setPolicyNumber("POL-AUTO-1001");
        request.setClaimType(ClaimType.ACCIDENT);
        request.setClaimedAmount(new BigDecimal("500.00"));
        request.setIncidentDate(LocalDate.now().minusDays(1));

        when(policyRepository.findByPolicyNumber("POL-AUTO-1001")).thenReturn(Optional.of(policy));
        when(claimNumberGeneratorService.generateClaimNumber()).thenReturn("CLM-20260101-0001");
        when(adjusterAssignmentService.findLeastLoadedAdjuster(anyString())).thenReturn(Optional.empty());
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponseDto responseDto = new ClaimResponseDto();
        responseDto.setClaimNumber("CLM-20260101-0001");
        when(claimMapper.toDto(any(Claim.class))).thenReturn(responseDto);

        ClaimResponseDto result = claimService.submitClaim(request);

        assertEquals("CLM-20260101-0001", result.getClaimNumber());
        verify(claimRepository).save(any(Claim.class));
        verify(notificationService).notifyClaimSubmitted(any(Claim.class));
    }
}
