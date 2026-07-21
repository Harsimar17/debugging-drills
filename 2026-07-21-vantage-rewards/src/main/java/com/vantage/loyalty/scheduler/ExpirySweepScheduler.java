package com.vantage.loyalty.scheduler;

import com.vantage.loyalty.service.PointsExpiryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpirySweepScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpirySweepScheduler.class);

    private final PointsExpiryService expiryService;

    public ExpirySweepScheduler(PointsExpiryService expiryService) {
        this.expiryService = expiryService;
    }

    /**
     * Runs nightly at 02:15 to expire points that have passed their validity
     * window. Also exposed manually via the admin endpoint for support.
     */
    @Scheduled(cron = "0 15 2 * * *")
    public void runNightlySweep() {
        try {
            int expired = expiryService.sweepExpiredPoints();
            log.info("Nightly expiry sweep expired {} entrie(s)", expired);
        } catch (RuntimeException ex) {
            log.error("Nightly expiry sweep failed", ex);
        }
    }
}
