package com.claimsflow.api;

import com.claimsflow.dto.ClaimRequestDto;
import com.claimsflow.dto.ClaimResponseDto;
import com.claimsflow.dto.ClaimStatusUpdateDto;
import com.claimsflow.service.ClaimService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private static final Logger log = LoggerFactory.getLogger(ClaimController.class);

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ResponseEntity<ClaimResponseDto> submitClaim(@Valid @RequestBody ClaimRequestDto request) {
        log.info("Received claim submission for policy {}", request.getPolicyNumber());
        ClaimResponseDto response = claimService.submitClaim(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{claimNumber}")
    public ResponseEntity<ClaimResponseDto> getClaim(@PathVariable String claimNumber) {
        return ResponseEntity.ok(claimService.getClaim(claimNumber));
    }

    @GetMapping
    public ResponseEntity<List<ClaimResponseDto>> getAllClaims(
            @RequestParam(required = false) String policyNumber) {
        if (policyNumber != null) {
            return ResponseEntity.ok(claimService.getClaimsForPolicy(policyNumber));
        }
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    @PatchMapping("/{claimNumber}/status")
    public ResponseEntity<ClaimResponseDto> updateStatus(
            @PathVariable String claimNumber,
            @Valid @RequestBody ClaimStatusUpdateDto update) {
        return ResponseEntity.ok(claimService.updateStatus(claimNumber, update));
    }
}
