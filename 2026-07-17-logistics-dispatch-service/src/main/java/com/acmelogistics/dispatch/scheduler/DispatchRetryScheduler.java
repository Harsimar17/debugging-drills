package com.acmelogistics.dispatch.scheduler;

import com.acmelogistics.dispatch.domain.Shipment;
import com.acmelogistics.dispatch.domain.ShipmentStatus;
import com.acmelogistics.dispatch.repository.ShipmentRepository;
import com.acmelogistics.dispatch.service.ShipmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Periodically re-attempts shipments that failed carrier dispatch or are
 * awaiting retry, so transient carrier outages don't require manual
 * intervention.
 */
@Component
public class DispatchRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(DispatchRetryScheduler.class);

    private final ShipmentRepository shipmentRepository;
    private final ShipmentService shipmentService;

    public DispatchRetryScheduler(ShipmentRepository shipmentRepository, ShipmentService shipmentService) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentService = shipmentService;
    }

    @Value("${dispatch.retry.max-attempts:5}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${dispatch.retry.fixed-delay-ms:10000}")
    public void retryFailedShipments() {
        List<Shipment> candidates = shipmentRepository.findByStatus(ShipmentStatus.PENDING_RETRY);
        if (candidates.isEmpty()) {
            return;
        }

        log.info("Found {} shipment(s) pending retry", candidates.size());

        for (Shipment shipment : candidates) {
            if (shipment.getDispatchAttempts() >= maxAttempts) {
                log.warn("Shipment {} exceeded max retry attempts ({}), leaving as failed",
                        shipment.getId(), maxAttempts);
                continue;
            }
            shipmentService.retryFailedDispatch(shipment);
        }
    }
}
