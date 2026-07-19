package com.acme.billing.service;

import com.acme.billing.domain.Invoice;
import com.acme.billing.domain.Subscription;
import com.acme.billing.domain.SubscriptionStatus;
import com.acme.billing.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Drives the recurring billing renewal process. Subscriptions whose next billing
 * date has arrived are invoiced and rolled forward to their next cycle.
 * <p>
 * Renewals are processed in parallel since a single billing run can cover
 * thousands of subscriptions and the payment gateway call per subscription
 * dominates wall-clock time.
 */
@Service
public class BillingRenewalService {

    private static final Logger log = LoggerFactory.getLogger(BillingRenewalService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceService invoiceService;
    private final NotificationService notificationService;

    /**
     * Tracks the subscriptions already renewed today so that if the batch is
     * triggered more than once in the same day (e.g. an operator re-running it
     * after a manual retry, or the job overlapping with itself under load) we
     * don't bill a customer twice for the same period.
     */
    private final Map<Long, LocalDate> lastRenewedOn = new HashMap<>();

    public BillingRenewalService(SubscriptionRepository subscriptionRepository,
                                  InvoiceService invoiceService,
                                  NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceService = invoiceService;
        this.notificationService = notificationService;
    }

    public BatchRunSummary runRenewalBatch() {
        LocalDate today = LocalDate.now();
        List<Subscription> due = subscriptionRepository.findDueForRenewal(SubscriptionStatus.ACTIVE, today);
        log.info("Renewal batch starting: {} subscriptions due for renewal as of {}", due.size(), today);

        AtomicInteger renewed = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        due.parallelStream().forEach(subscription -> {
            try {
                renewSubscription(subscription, today, renewed, skipped);
            } catch (Exception ex) {
                failed.incrementAndGet();
                log.error("Failed to renew subscription {}", subscription.getId(), ex);
            }
        });

        BatchRunSummary summary = new BatchRunSummary(due.size(), renewed.get(), skipped.get(), failed.get());
        log.info("Renewal batch finished: {}", summary);
        return summary;
    }

    private void renewSubscription(Subscription subscription, LocalDate today,
                                    AtomicInteger renewed, AtomicInteger skipped) {
        LocalDate alreadyRenewed = lastRenewedOn.get(subscription.getId());
        if (today.equals(alreadyRenewed)) {
            log.debug("Subscription {} already renewed today, skipping duplicate run", subscription.getId());
            skipped.incrementAndGet();
            return;
        }

        // Simulates the latency of calling out to the payment gateway / invoicing provider.
        simulateGatewayLatency();

        Invoice invoice = invoiceService.generateInvoiceForRenewal(subscription);
        advanceSubscription(subscription);
        lastRenewedOn.put(subscription.getId(), today);
        notificationService.sendInvoiceNotification(subscription.getCustomer(), invoice);
        renewed.incrementAndGet();
    }

    @Transactional
    protected void advanceSubscription(Subscription subscription) {
        LocalDate nextDate = subscription.getPlan().getBillingCycle().advance(subscription.getNextBillingDate());
        subscription.setNextBillingDate(nextDate);
        subscriptionRepository.save(subscription);
    }

    private void simulateGatewayLatency() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(80, 260));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public record BatchRunSummary(int due, int renewed, int skipped, int failed) {
    }
}
