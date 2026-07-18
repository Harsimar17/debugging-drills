package com.claimsflow.scheduler;

import com.claimsflow.domain.Claim;
import com.claimsflow.domain.enums.ClaimStatus;
import com.claimsflow.repository.ClaimRepository;
import com.claimsflow.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ClaimEscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ClaimEscalationScheduler.class);
    private static final int ESCALATION_THRESHOLD_HOURS = 72;

    private final ClaimRepository claimRepository;
    private final NotificationService notificationService;

    public ClaimEscalationScheduler(ClaimRepository claimRepository, NotificationService notificationService) {
        this.claimRepository = claimRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void escalateStaleClaims() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(ESCALATION_THRESHOLD_HOURS);
        List<Claim> staleClaims = claimRepository.findStaleClaims(ClaimStatus.UNDER_REVIEW, cutoff);

        if (staleClaims.isEmpty()) {
            log.debug("No stale claims found during escalation sweep.");
            return;
        }

        log.info("Found {} stale claims to escalate.", staleClaims.size());
        for (Claim claim : staleClaims) {
            notificationService.notifyEscalation(claim);
        }
    }
}
