package com.akeshya.dto.response;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private Double itemTotal;
    private String selectedSize;
    private String selectedColor;
}