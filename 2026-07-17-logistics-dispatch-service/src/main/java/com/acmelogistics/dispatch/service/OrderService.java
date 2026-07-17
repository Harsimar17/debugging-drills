package com.acmelogistics.dispatch.service;

import com.acmelogistics.dispatch.common.ReferenceNumberGenerator;
import com.acmelogistics.dispatch.domain.Order;
import com.acmelogistics.dispatch.domain.OrderStatus;
import com.acmelogistics.dispatch.dto.OrderRequest;
import com.acmelogistics.dispatch.dto.OrderResponse;
import com.acmelogistics.dispatch.exception.OrderNotFoundException;
import com.acmelogistics.dispatch.mapper.OrderMapper;
import com.acmelogistics.dispatch.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ReferenceNumberGenerator referenceNumberGenerator;

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper,
                         ReferenceNumberGenerator referenceNumberGenerator) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.referenceNumberGenerator = referenceNumberGenerator;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Order order = Order.builder()
                .orderNumber(referenceNumberGenerator.nextOrderNumber())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .deliveryAddress(request.getDeliveryAddress())
                .totalAmount(request.getTotalAmount())
                .orderStatus(OrderStatus.RECEIVED)
                .build();

        Order saved = orderRepository.save(order);
        log.info("Created order {} for customer {}", saved.getOrderNumber(), saved.getCustomerEmail());
        return orderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return orderMapper.toResponse(order);
    }

    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setOrderStatus(status);
        orderRepository.save(order);
    }
}
