package com.akeshya.service.impl;

import com.akeshya.dto.request.OrderItemRequest;
import com.akeshya.dto.request.OrderRequest;
import com.akeshya.dto.response.OrderItemResponse;
import com.akeshya.dto.response.OrderResponse;
import com.akeshya.dto.response.OrderTrackingResponse;
import com.akeshya.entity.*;
import com.akeshya.repository.OrderRepository;
import com.akeshya.repository.OrderTrackingRepository;
import com.akeshya.repository.ProductRepository;
import com.akeshya.repository.UserRepository;
import com.akeshya.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderTrackingRepository orderTrackingRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ResponseEntity<?> createOrder(OrderRequest orderRequest) {
        try {
        	
        	User users = userRepository.findById(orderRequest.getUserId()).orElseThrow(()->new RuntimeException("user not found"));
           
         
      

            // Create order
            Order order = Order.builder()
                    .orderNumber(generateOrderNumber())
                    .user(users)
                    .shippingAddress(users.getShippingAddress())
                    .contactPersonName(users.getContactPersonName())
                    .contactNumber(users.getContactNumber())
                    .specialInstructions(orderRequest.getInstruction())
                    .status(OrderStatus.PENDING)
                    .build();

            double totalAmount = processOrderItems(order, orderRequest.getItems());
            order.setTotalAmount(totalAmount);

            // Save order first to get ID
            Order savedOrder = orderRepository.save(order);

            // Create initial tracking entry
            OrderTracking initialTracking = OrderTracking.builder()
                    .order(savedOrder)
                    .status(OrderStatus.PENDING)
                    .description("Order placed successfully")
                    .build();
            orderTrackingRepository.save(initialTracking);

            log.info("Order created: {}", savedOrder.getOrderNumber());

            // Convert to DTO before returning
            OrderResponse orderResponse = convertToOrderResponse(savedOrder);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);

        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error creating order: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOrderById(Long orderId) {
        try {
            Optional<Order> order = orderRepository.findById(orderId);
            if (order.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Convert to DTO
            OrderResponse orderResponse = convertToOrderResponse(order.get());
            return ResponseEntity.ok(orderResponse);
            
        } catch (Exception e) {
            log.error("Error fetching order: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error fetching order");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOrderByNumber(String orderNumber) {
        try {
            Optional<Order> order = orderRepository.findByOrderNumber(orderNumber);
            if (order.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Convert to DTO
            OrderResponse orderResponse = convertToOrderResponse(order.get());
            return ResponseEntity.ok(orderResponse);
            
        } catch (Exception e) {
            log.error("Error fetching order: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error fetching order");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUserOrders(UUID userId) {
        try {
            List<Order> orders = orderRepository.findByUserId(userId);
            
            // Convert to DTOs
            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(orderResponses);
            
        } catch (Exception e) {
            log.error("Error fetching user orders: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error fetching orders");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateOrderStatus(Long orderId, OrderStatus status, String notes) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            
            // Update order status
            order.setStatus(status);
            Order updatedOrder = orderRepository.save(order);

            // Create tracking entry
            String description = notes != null ? notes : "Status updated to " + status;
            OrderTracking tracking = OrderTracking.builder()
                    .order(updatedOrder)
                    .status(status)
                    .description(description)
                    .build();
            orderTrackingRepository.save(tracking);
            
            // Convert to DTO
            OrderResponse orderResponse = convertToOrderResponse(updatedOrder);
            return ResponseEntity.ok(orderResponse);

        } catch (Exception e) {
            log.error("Error updating order status: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error updating status");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> cancelOrder(Long orderId, String reason) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            if (order.getStatus() == OrderStatus.CANCELLED) {
                return ResponseEntity.badRequest().body("Order already cancelled");
            }

            if (order.getStatus() == OrderStatus.DELIVERED) {
                return ResponseEntity.badRequest().body("Cannot cancel delivered order");
            }

            // Update order status
            order.setStatus(OrderStatus.CANCELLED);
            Order cancelledOrder = orderRepository.save(order);

            // Create tracking entry
            String cancelReason = reason != null ? "Order cancelled: " + reason : "Order cancelled";
            OrderTracking tracking = OrderTracking.builder()
                    .order(cancelledOrder)
                    .status(OrderStatus.CANCELLED)
                    .description(cancelReason)
                    .build();
            orderTrackingRepository.save(tracking);
            
            // Convert to DTO
            OrderResponse orderResponse = convertToOrderResponse(cancelledOrder);
            return ResponseEntity.ok(orderResponse);

        } catch (Exception e) {
            log.error("Error cancelling order: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error cancelling order");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOrderTracking(Long orderId) {
        try {
            // Check if order exists
            boolean orderExists = orderRepository.existsById(orderId);
            if (!orderExists) {
                return ResponseEntity.notFound().build();
            }

            // Get tracking entries for this order
            List<OrderTracking> trackingEntries = orderTrackingRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
            
            // Convert to DTOs
            List<OrderTrackingResponse> trackingResponses = trackingEntries.stream()
                    .map(this::convertToOrderTrackingResponse)
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(trackingResponses);

        } catch (Exception e) {
            log.error("Error fetching order tracking: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error fetching tracking");
        }
    }

    // Helper methods
    private double processOrderItems(Order order, List<OrderItemRequest> items) {
        double total = 0;
        
        for (OrderItemRequest itemRequest : items) {
            Optional<Product> productOpt = productRepository.findById(itemRequest.getProductId());
            if (productOpt.isEmpty()) {
                throw new RuntimeException("Product not found: " + itemRequest.getProductId());
            }

            Product product = productOpt.get();
            if (!product.getIsPublished()) {
                throw new RuntimeException("Product not available: " + product.getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .selectedSize(itemRequest.getSelectedSize())
                    .selectedColor(itemRequest.getSelectedColor())
                    .build();

            orderItem.calculateTotal();
            order.getItems().add(orderItem);
            total += orderItem.getItemTotal();
        }
        
        return total;
    }

    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis();
    }

    // DTO Conversion Methods
    private OrderResponse convertToOrderResponse(Order order) {
        // Get tracking entries for this order
        List<OrderTracking> trackingEntries = orderTrackingRepository.findByOrderIdOrderByCreatedAtAsc(order.getId());
        
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());

        List<OrderTrackingResponse> trackingResponses = trackingEntries.stream()
                .map(this::convertToOrderTrackingResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .shippingAddress(order.getShippingAddress())
                .contactPersonName(order.getContactPersonName())
                .contactNumber(order.getContactNumber())
                .specialInstructions(order.getSpecialInstructions())
                .items(itemResponses)
                .tracking(trackingResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse convertToOrderItemResponse(OrderItem item) {
        String productName = item.getProduct() != null ? item.getProduct().getName() : "Unknown Product";
        Long productId = item.getProduct() != null ? item.getProduct().getId() : null;
        
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(productId)
                .productName(productName)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .itemTotal(item.getItemTotal())
                .selectedSize(item.getSelectedSize())
                .selectedColor(item.getSelectedColor())
                .build();
    }

    private OrderTrackingResponse convertToOrderTrackingResponse(OrderTracking tracking) {
        return OrderTrackingResponse.builder()
                .id(tracking.getId())
                .status(tracking.getStatus().name())
                .description(tracking.getDescription())
                .createdAt(tracking.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
            
            // Convert to DTOs
            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(orderResponses);
            
        } catch (Exception e) {
            log.error("Error fetching all orders: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error fetching orders: " + e.getMessage());
        }
    }
}