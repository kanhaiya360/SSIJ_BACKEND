package com.akeshya.dto.request;

import com.akeshya.entity.OrderStatus;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    private OrderStatus status;
    private String notes;
}