package com.akeshya.dto.request;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequest {
    private String name;
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    private String status;
    private Boolean isPublished;
    private List<String> colors = new ArrayList<>();
    private List<ProductSizeRequest> sizes = new ArrayList<>();
    private List<ProductImageRequest> images = new ArrayList<>();
    
}