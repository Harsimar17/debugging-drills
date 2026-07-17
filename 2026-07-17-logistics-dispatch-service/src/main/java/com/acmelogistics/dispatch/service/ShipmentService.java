package com.acmelogistics.dispatch.service;

import com.acmelogistics.dispatch.common.ReferenceNumberGenerator;
import com.acmelogistics.dispatch.domain.DispatchEventType;
import com.acmelogistics.dispatch.domain.Order;
import com.acmelogistics.dispatch.domain.Shipment;
import com.acmelogistics.dispatch.domain.ShipmentStatus;
import com.acmelogistics.dispatch.dto.CarrierQuote;
import com.acmelogistics.dispatch.dto.ShipmentRequest;
import com.acmelogistics.dispatch.dto.ShipmentResponse;
import com.acmelogistics.dispatch.exception.OrderNotFoundException;
import com.acmelogistics.dispatch.exception.ShipmentNotFoundException;
import com.acmelogistics.dispatch.mapper.ShipmentMapper;
import com.acmelogistics.dispatch.repository.OrderRepository;
import com.acmelogistics.dispatch.repository.ShipmentRepository;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ShipmentMapper shipmentMapper;
    private final ReferenceNumberGenerator referenceNumberGenerator;
    private final CarrierIntegrationService carrierIntegrationService;
    private final DispatchEventService dispatchEventService;
    private final IdempotencyService idempotencyService;

    public ShipmentService(ShipmentRepository shipmentRepository, OrderRepository orderRepository,
                            ShipmentMapper shipmentMapper, ReferenceNumberGenerator referenceNumberGenerator,
                            CarrierIntegrationService carrierIntegrationService,
                            DispatchEventService dispatchEventService, IdempotencyService idempotencyService) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.shipmentMapper = shipmentMapper;
        this.referenceNumberGenerator = referenceNumberGenerator;
        this.carrierIntegrationService = carrierIntegrationService;
        this.dispatchEventService = dispatchEventService;
        this.idempotencyService = idempotencyService;
    }

    public ShipmentResponse dispatchShipment(String idempotencyKey, ShipmentRequest request) {
        Optional<Shipment> existing = shipmentRepository.findFirstByIdempotencyKeyOrderByIdAsc(idempotencyKey);
        if (existing.isPresent()) {
            return shipmentMapper.toResponse(existing.get());
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(request.getOrderId()));

        CarrierQuote quote = carrierIntegrationService.getQuote(request.getWeightKg(), request.getPreferredCarrierId());

        Shipment shipment = Shipment.builder()
                .orderId(order.getId())
                .idempotencyKey(idempotencyKey)
                .carrierId(quote.getCarrierId())
                .trackingNumber(referenceNumberGenerator.nextTrackingNumber(quote.getCarrierCode()))
                .status(ShipmentStatus.DISPATCHED)
                .weightKg(request.getWeightKg())
                .shippingCost(quote.getCost())
                .dispatchAttempts(1)
                .build();

        try {
            Shipment saved = shipmentRepository.saveAndFlush(shipment);
            dispatchEventService.record(saved.getId(), DispatchEventType.SHIPMENT_CREATED,
                    "Shipment created for order " + order.getOrderNumber());
            dispatchEventService.record(saved.getId(), DispatchEventType.CARRIER_NOTIFIED,
                    "Carrier " + quote.getCarrierCode() + " notified, tracking " + saved.getTrackingNumber());
            return shipmentMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            // Another concurrent request won the insert race for this key — return its result.
            return shipmentRepository.findFirstByIdempotencyKeyOrderByIdAsc(idempotencyKey)
                    .map(shipmentMapper::toResponse)
                    .orElseThrow(() -> ex);
        }
    }

    @Transactional
    public void retryFailedDispatch(Shipment shipment) {
        log.info("Retrying dispatch for shipment {} (attempt {})",
                shipment.getId(), shipment.getDispatchAttempts() + 1);

        ShipmentRequest retryRequest = new ShipmentRequest();
        retryRequest.setOrderId(shipment.getOrderId());
        retryRequest.setWeightKg(shipment.getWeightKg());
        retryRequest.setPreferredCarrierId(shipment.getCarrierId());

        try {
            CarrierQuote quote = carrierIntegrationService.getQuote(shipment.getWeightKg(), shipment.getCarrierId());
            shipment.setCarrierId(quote.getCarrierId());
            shipment.setShippingCost(quote.getCost());
            shipment.setStatus(ShipmentStatus.DISPATCHED);
            shipment.setDispatchAttempts(shipment.getDispatchAttempts() + 1);
            shipmentRepository.save(shipment);

            dispatchEventService.record(shipment.getId(), DispatchEventType.CARRIER_NOTIFIED,
                    "Retry succeeded via carrier " + quote.getCarrierCode());
        } catch (Exception ex) {
            shipment.setDispatchAttempts(shipment.getDispatchAttempts() + 1);
            shipment.setStatus(ShipmentStatus.FAILED);
            shipmentRepository.save(shipment);
            dispatchEventService.record(shipment.getId(), DispatchEventType.DISPATCH_FAILED,
                    "Retry failed: " + ex.getMessage());
            log.error("Retry failed for shipment {}: {}", shipment.getId(), ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipment(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));
        return shipmentMapper.toResponse(shipment);
    }
}
