package com.claimsflow.service;

import com.claimsflow.domain.Claim;
import com.claimsflow.domain.ClaimStatusHistory;
import com.claimsflow.domain.enums.ClaimStatus;
import com.claimsflow.exception.InvalidClaimStateException;
import com.claimsflow.repository.ClaimStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class ClaimStatusService {

    private static final Logger log = LoggerFactory.getLogger(ClaimStatusService.class);

    private static final Map<ClaimStatus, Set<ClaimStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ClaimStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ClaimStatus.SUBMITTED, EnumSet.of(ClaimStatus.UNDER_REVIEW, ClaimStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.UNDER_REVIEW, EnumSet.of(ClaimStatus.PENDING_DOCUMENTS, ClaimStatus.APPROVED, ClaimStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.PENDING_DOCUMENTS, EnumSet.of(ClaimStatus.UNDER_REVIEW, ClaimStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.APPROVED, EnumSet.of(ClaimStatus.PAID));
        ALLOWED_TRANSITIONS.put(ClaimStatus.REJECTED, EnumSet.of(ClaimStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.PAID, EnumSet.of(ClaimStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.CLOSED, EnumSet.noneOf(ClaimStatus.class));
    }

    private final ClaimStatusHistoryRepository historyRepository;

    public ClaimStatusService(ClaimStatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public void transition(Claim claim, ClaimStatus newStatus, String changedBy, String notes) {
        ClaimStatus current = claim.getStatus();
        Set<ClaimStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());

        if (!allowed.contains(newStatus)) {
            throw new InvalidClaimStateException(
                    "Cannot transition claim " + claim.getClaimNumber() + " from " + current + " to " + newStatus);
        }

        ClaimStatusHistory history = new ClaimStatusHistory();
        history.setClaim(claim);
        history.setPreviousStatus(current);
        history.setNewStatus(newStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(changedBy);
        history.setNotes(notes);
        historyRepository.save(history);

        claim.setStatus(newStatus);
        claim.setLastUpdatedAt(LocalDateTime.now());

        log.info("Claim {} transitioned {} -> {}", claim.getClaimNumber(), current, newStatus);
    }
}
