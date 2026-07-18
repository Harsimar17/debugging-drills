package com.claimsflow.repository;

import com.claimsflow.domain.Claim;
import com.claimsflow.domain.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    List<Claim> findByStatus(ClaimStatus status);

    List<Claim> findByPolicyId(Long policyId);

    @Query("select c from Claim c where c.status = :status and c.submittedAt < :cutoff")
    List<Claim> findStaleClaims(ClaimStatus status, java.time.LocalDateTime cutoff);
}
