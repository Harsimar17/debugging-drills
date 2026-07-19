package com.acme.billing.api;

import com.acme.billing.dto.ApiResponse;
import com.acme.billing.service.BillingRenewalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/billing")
public class AdminBillingController {

    private static final Logger log = LoggerFactory.getLogger(AdminBillingController.class);

    private final BillingRenewalService billingRenewalService;

    public AdminBillingController(BillingRenewalService billingRenewalService) {
        this.billingRenewalService = billingRenewalService;
    }

    /**
     * Lets support/ops trigger the renewal batch on demand, e.g. to process a
     * customer's renewal immediately instead of waiting for the nightly cron.
     */
    @PostMapping("/renewals/run")
    public ApiResponse<BillingRenewalService.BatchRunSummary> runRenewalNow() {
        log.info("Renewal batch manually triggered via admin API");
        return ApiResponse.ok(billingRenewalService.runRenewalBatch());
    }
}
