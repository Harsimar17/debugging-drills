package com.claimsflow.service;

import com.claimsflow.domain.Claim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Async
    public void notifyClaimSubmitted(Claim claim) {
        log.info("Notification: claim {} submitted for policy {}",
                claim.getClaimNumber(), claim.getPolicy().getPolicyNumber());
    }

    @Async
    public void notifyStatusChanged(Claim claim, String previousStatus) {
        log.info("Notification: claim {} status changed from {} to {}",
                claim.getClaimNumber(), previousStatus, claim.getStatus());
    }

    @Async
    public void notifyEscalation(Claim claim) {
        log.warn("Notification: claim {} escalated due to inactivity", claim.getClaimNumber());
    }
}
