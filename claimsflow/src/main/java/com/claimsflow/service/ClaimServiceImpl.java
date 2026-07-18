package com.claimsflow.service;

import com.claimsflow.domain.Claim;
import com.claimsflow.domain.Policy;
import com.claimsflow.dto.ClaimRequestDto;
import com.claimsflow.dto.ClaimResponseDto;
import com.claimsflow.dto.ClaimStatusUpdateDto;
import com.claimsflow.exception.ClaimNotFoundException;
import com.claimsflow.exception.PolicyNotFoundException;
import com.claimsflow.mapper.ClaimMapper;
import com.claimsflow.repository.ClaimRepository;
import com.claimsflow.repository.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimServiceImpl implements ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimServiceImpl.class);

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final ClaimMapper claimMapper;
    private final ClaimNumberGeneratorService claimNumberGeneratorService;
    private final AdjusterAssignmentService adjusterAssignmentService;
    private final ClaimStatusService claimStatusService;
    private final NotificationService notificationService;

    public ClaimServiceImpl(ClaimRepository claimRepository,
                             PolicyRepository policyRepository,
                             ClaimMapper claimMapper,
                             ClaimNumberGeneratorService claimNumberGeneratorService,
                             AdjusterAssignmentService adjusterAssignmentService,
                             ClaimStatusService claimStatusService,
                             NotificationService notificationService) {
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
        this.claimMapper = claimMapper;
        this.claimNumberGeneratorService = claimNumberGeneratorService;
        this.adjusterAssignmentService = adjusterAssignmentService;
        this.claimStatusService = claimStatusService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public ClaimResponseDto submitClaim(ClaimRequestDto request) {
        Policy policy = policyRepository.findByPolicyNumber(request.getPolicyNumber())
                .orElseThrow(() -> new PolicyNotFoundException(
                        "No policy found with number " + request.getPolicyNumber()));

        if (!policy.isActive()) {
            log.warn("Claim submitted against inactive policy {}", policy.getPolicyNumber());
        }

        Claim claim = new Claim();
        claim.setClaimNumber(claimNumberGeneratorService.generateClaimNumber());
        claim.setClaimType(request.getClaimType());
        claim.setClaimedAmount(request.getClaimedAmount());
        claim.setDescription(request.getDescription());
        claim.setIncidentDate(request.getIncidentDate());
        claim.setSubmittedAt(LocalDateTime.now());
        claim.setPolicy(policy);

        adjusterAssignmentService.findLeastLoadedAdjuster(inferRegion(policy))
                .ifPresent(adjuster -> adjusterAssignmentService.assign(claim, adjuster));

        Claim saved = claimRepository.save(claim);
        log.info("Claim {} submitted for policy {}", saved.getClaimNumber(), policy.getPolicyNumber());

        notificationService.notifyClaimSubmitted(saved);

        return claimMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimResponseDto getClaim(String claimNumber) {
        Claim claim = claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new ClaimNotFoundException("No claim found with number " + claimNumber));
        return claimMapper.toDto(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponseDto> getClaimsForPolicy(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new PolicyNotFoundException("No policy found with number " + policyNumber));

        return claimRepository.findByPolicyId(policy.getId()).stream()
                .map(claimMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClaimResponseDto updateStatus(String claimNumber, ClaimStatusUpdateDto update) {
        Claim claim = claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new ClaimNotFoundException("No claim found with number " + claimNumber));

        String previousStatus = claim.getStatus().name();

        if (update.getApprovedAmount() != null) {
            claim.setApprovedAmount(update.getApprovedAmount());
        }

        claimStatusService.transition(claim, update.getNewStatus(), update.getChangedBy(), update.getNotes());
        Claim saved = claimRepository.save(claim);

        notificationService.notifyStatusChanged(saved, previousStatus);

        return claimMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponseDto> getAllClaims() {
        return claimRepository.findAll().stream()
                .map(claimMapper::toDto)
                .collect(Collectors.toList());
    }

    private String inferRegion(Policy policy) {
        return "NORTHEAST";
    }
}
