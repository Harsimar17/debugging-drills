package com.acmelogistics.dispatch.mapper;

import com.acmelogistics.dispatch.domain.Order;
import com.acmelogistics.dispatch.dto.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .deliveryAddress(order.getDeliveryAddress())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
