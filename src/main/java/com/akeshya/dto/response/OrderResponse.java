package com.akeshya.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.akeshya.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Double totalAmount;
    private String status;
    private String shippingAddress;
    private String contactPersonName;
    private String contactNumber;
    private String specialInstructions;
    private List<OrderItemResponse> items;
    private List<OrderTrackingResponse> tracking;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}