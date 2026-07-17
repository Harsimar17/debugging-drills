package com.acmelogistics.dispatch.api;

import com.acmelogistics.dispatch.common.Constants;
import com.acmelogistics.dispatch.dto.ShipmentRequest;
import com.acmelogistics.dispatch.dto.ShipmentResponse;
import com.acmelogistics.dispatch.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> dispatchShipment(
            @RequestHeader(value = Constants.IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody ShipmentRequest request) {

        String key = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey
                : UUID.randomUUID().toString();

        ShipmentResponse response = shipmentService.dispatchShipment(key, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(shipmentService.getShipment(shipmentId));
    }
}
