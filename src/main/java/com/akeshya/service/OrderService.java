package com.akeshya.service;

import com.akeshya.dto.request.OrderRequest;
import com.akeshya.entity.Order;
import com.akeshya.entity.OrderStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    ResponseEntity<?> createOrder(OrderRequest orderRequest);
    ResponseEntity<?> getOrderById(Long orderId);
    ResponseEntity<?> getOrderByNumber(String orderNumber);
    ResponseEntity<?> getUserOrders(UUID userId);
    ResponseEntity<?> updateOrderStatus(Long orderId, OrderStatus status, String notes);
    ResponseEntity<?> cancelOrder(Long orderId, String reason);
    ResponseEntity<?> getOrderTracking(Long orderId);
	ResponseEntity<?> getAllOrders();
}