package com.akeshya.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OrderRequest {
	
	private UUID userId;
    @NotEmpty
    private List<OrderItemRequest> items;
    private String instruction;
}