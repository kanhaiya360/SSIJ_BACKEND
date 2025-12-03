package com.akeshya.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotBlank
    private String shippingAddress;
    
    private String contactPersonName;
    
    @NotBlank
    private String contactNumber;
    
    private String specialInstructions;
    
    @NotEmpty
    private List<OrderItemRequest> items;
}