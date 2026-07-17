package com.acmelogistics.dispatch.api;

import com.acmelogistics.dispatch.domain.DispatchEventType;
import com.acmelogistics.dispatch.domain.Shipment;
import com.acmelogistics.dispatch.domain.ShipmentStatus;
import com.acmelogistics.dispatch.dto.CarrierWebhookPayload;
import com.acmelogistics.dispatch.exception.ShipmentNotFoundException;
import com.acmelogistics.dispatch.repository.ShipmentRepository;
import com.acmelogistics.dispatch.service.DispatchEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/carrier")
public class CarrierWebhookController {

    private static final Logger log = LoggerFactory.getLogger(CarrierWebhookController.class);

    private final ShipmentRepository shipmentRepository;
    private final DispatchEventService dispatchEventService;

    public CarrierWebhookController(ShipmentRepository shipmentRepository, DispatchEventService dispatchEventService) {
        this.shipmentRepository = shipmentRepository;
        this.dispatchEventService = dispatchEventService;
    }

    @PostMapping("/status")
    @Transactional
    public ResponseEntity<Void> receiveStatusUpdate(@RequestBody CarrierWebhookPayload payload) {
        log.info("Received carrier webhook: tracking={}, event={}", payload.getTrackingNumber(), payload.getEventCode());

        Shipment shipment = shipmentRepository.findByTrackingNumber(payload.getTrackingNumber())
                .orElseThrow(() -> new ShipmentNotFoundException(
                        "No shipment found for tracking number " + payload.getTrackingNumber()));

        if ("DELIVERED".equalsIgnoreCase(payload.getEventCode())) {
            shipment.setStatus(ShipmentStatus.DELIVERED);
            shipmentRepository.save(shipment);
            dispatchEventService.record(shipment.getId(), DispatchEventType.DELIVERY_CONFIRMED,
                    "Carrier confirmed delivery at " + payload.getRawTimestamp());
        }

        return ResponseEntity.ok().build();
    }
}
