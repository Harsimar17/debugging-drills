package com.acmelogistics.dispatch.api;

import com.acmelogistics.dispatch.common.Constants;
import com.acmelogistics.dispatch.domain.Order;
import com.acmelogistics.dispatch.domain.OrderStatus;
import com.acmelogistics.dispatch.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShipmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long orderId;

    @BeforeEach
    void setUp() {
        Order order = Order.builder()
                .orderNumber("TEST-ORD-1")
                .customerName("Test Customer")
                .customerEmail("test@example.com")
                .deliveryAddress("1 Test Way")
                .orderStatus(OrderStatus.RECEIVED)
                .totalAmount(BigDecimal.valueOf(20))
                .build();
        orderId = orderRepository.save(order).getId();
    }

    @Test
    void dispatchingAShipmentReturnsCreated() throws Exception {
        Map<String, Object> payload = Map.of(
                "orderId", orderId,
                "weightKg", 3.5
        );

        mockMvc.perform(post("/api/shipments")
                        .header(Constants.IDEMPOTENCY_KEY_HEADER, "integration-test-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());
    }
}
