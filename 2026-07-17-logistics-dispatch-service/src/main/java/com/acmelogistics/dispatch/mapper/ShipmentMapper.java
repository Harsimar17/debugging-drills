package com.acmelogistics.dispatch.mapper;

import com.acmelogistics.dispatch.domain.Shipment;
import com.acmelogistics.dispatch.dto.ShipmentResponse;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMapper {

    public ShipmentResponse toResponse(Shipment shipment) {
        if (shipment == null) {
            return null;
        }
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrderId())
                .carrierId(shipment.getCarrierId())
                .trackingNumber(shipment.getTrackingNumber())
                .status(shipment.getStatus())
                .weightKg(shipment.getWeightKg())
                .shippingCost(shipment.getShippingCost())
                .dispatchAttempts(shipment.getDispatchAttempts())
                .createdAt(shipment.getCreatedAt())
                .build();
    }
}
