package com.claimsflow.service;

import com.claimsflow.domain.Adjuster;
import com.claimsflow.domain.Claim;
import com.claimsflow.repository.AdjusterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AdjusterAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AdjusterAssignmentService.class);

    private final AdjusterRepository adjusterRepository;

    public AdjusterAssignmentService(AdjusterRepository adjusterRepository) {
        this.adjusterRepository = adjusterRepository;
    }

    public Optional<Adjuster> findLeastLoadedAdjuster(String region) {
        List<Adjuster> candidates = adjusterRepository.findByRegion(region);
        if (candidates.isEmpty()) {
            log.warn("No adjusters available for region {}", region);
            return Optional.empty();
        }

        return candidates.stream()
                .filter(a -> a.getAssignedClaims().size() < a.getMaxActiveClaims())
                .min(Comparator.comparingInt(a -> a.getAssignedClaims().size()));
    }

    public void assign(Claim claim, Adjuster adjuster) {
        claim.setAssignedAdjuster(adjuster);
        log.info("Claim {} assigned to adjuster {}", claim.getClaimNumber(), adjuster.getEmployeeCode());
    }
}
