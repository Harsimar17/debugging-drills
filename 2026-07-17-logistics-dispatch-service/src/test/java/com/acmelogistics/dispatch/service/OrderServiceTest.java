package com.acmelogistics.dispatch.service;

import com.acmelogistics.dispatch.common.ReferenceNumberGenerator;
import com.acmelogistics.dispatch.domain.Order;
import com.acmelogistics.dispatch.dto.OrderRequest;
import com.acmelogistics.dispatch.dto.OrderResponse;
import com.acmelogistics.dispatch.mapper.OrderMapper;
import com.acmelogistics.dispatch.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ReferenceNumberGenerator referenceNumberGenerator;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderPersistsAndReturnsMappedResponse() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Jane Doe");
        request.setCustomerEmail("jane@example.com");
        request.setDeliveryAddress("123 Main St");
        request.setTotalAmount(BigDecimal.valueOf(99.99));

        when(referenceNumberGenerator.nextOrderNumber()).thenReturn("ACME-ORD-1001");
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(1L);
            return o;
        });
        when(orderMapper.toResponse(any(Order.class))).thenReturn(
                OrderResponse.builder().id(1L).orderNumber("ACME-ORD-1001").build());

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOrderNumber()).isEqualTo("ACME-ORD-1001");
    }
}
