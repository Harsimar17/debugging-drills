package com.acme.billing.scheduler;

import com.acme.billing.service.BillingRenewalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionRenewalScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionRenewalScheduler.class);

    private final BillingRenewalService billingRenewalService;

    public SubscriptionRenewalScheduler(BillingRenewalService billingRenewalService) {
        this.billingRenewalService = billingRenewalService;
    }

    /**
     * Runs every day at 02:00 to bill any subscription due that day.
     * Operators can also trigger a run on demand via the admin API when a
     * customer's renewal needs to be processed immediately.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void runNightlyRenewal() {
        log.info("Nightly renewal job triggered by cron schedule");
        billingRenewalService.runRenewalBatch();
    }
}
