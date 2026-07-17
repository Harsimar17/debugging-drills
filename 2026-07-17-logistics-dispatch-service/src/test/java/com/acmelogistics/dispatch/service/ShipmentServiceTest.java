package com.acmelogistics.dispatch.service;

import com.acmelogistics.dispatch.common.ReferenceNumberGenerator;
import com.acmelogistics.dispatch.domain.Order;
import com.acmelogistics.dispatch.domain.OrderStatus;
import com.acmelogistics.dispatch.domain.Shipment;
import com.acmelogistics.dispatch.dto.CarrierQuote;
import com.acmelogistics.dispatch.dto.ShipmentRequest;
import com.acmelogistics.dispatch.dto.ShipmentResponse;
import com.acmelogistics.dispatch.mapper.ShipmentMapper;
import com.acmelogistics.dispatch.repository.OrderRepository;
import com.acmelogistics.dispatch.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ShipmentMapper shipmentMapper;
    @Mock
    private ReferenceNumberGenerator referenceNumberGenerator;
    @Mock
    private CarrierIntegrationService carrierIntegrationService;
    @Mock
    private DispatchEventService dispatchEventService;
    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private ShipmentService shipmentService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(1L)
                .orderNumber("ACME-ORD-1001")
                .orderStatus(OrderStatus.RECEIVED)
                .totalAmount(BigDecimal.valueOf(50))
                .build();
    }

    @Test
    void dispatchShipmentCreatesNewShipmentWhenNotDuplicate() {
        ShipmentRequest request = new ShipmentRequest();
        request.setOrderId(1L);
        request.setWeightKg(BigDecimal.valueOf(5));

        when(idempotencyService.isDuplicate("key-1")).thenReturn(false);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(carrierIntegrationService.getQuote(any(), any())).thenReturn(
                CarrierQuote.builder().carrierId(2L).carrierCode("SWFT").cost(BigDecimal.TEN).estimatedDeliveryDays(3).build());
        when(referenceNumberGenerator.nextTrackingNumber("SWFT")).thenReturn("ACME-TRK-SWFT-1");
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> {
            Shipment s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });
        when(shipmentMapper.toResponse(any(Shipment.class))).thenReturn(
                ShipmentResponse.builder().id(10L).orderId(1L).build());

        ShipmentResponse response = shipmentService.dispatchShipment("key-1", request);

        assertThat(response.getId()).isEqualTo(10L);
        verify(idempotencyService).markProcessed("key-1");
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    void dispatchShipmentReturnsExistingShipmentWhenDuplicate() {
        ShipmentRequest request = new ShipmentRequest();
        request.setOrderId(1L);
        request.setWeightKg(BigDecimal.valueOf(5));

        Shipment existing = Shipment.builder().id(99L).orderId(1L).idempotencyKey("key-2").build();

        when(idempotencyService.isDuplicate("key-2")).thenReturn(true);
        when(shipmentRepository.findFirstByIdempotencyKeyOrderByIdAsc("key-2")).thenReturn(Optional.of(existing));
        when(shipmentMapper.toResponse(existing)).thenReturn(ShipmentResponse.builder().id(99L).build());

        ShipmentResponse response = shipmentService.dispatchShipment("key-2", request);

        assertThat(response.getId()).isEqualTo(99L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }
}
