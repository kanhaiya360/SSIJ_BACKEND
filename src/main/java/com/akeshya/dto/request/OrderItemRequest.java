package com.akeshya.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull
    private Long productId;
    
    @NotNull
    @Min(1)
    private Integer quantity;
    
    @NotNull
    @Min(0)
    private Double unitPrice;
    
    private String selectedSize;
    private String selectedColor;
}