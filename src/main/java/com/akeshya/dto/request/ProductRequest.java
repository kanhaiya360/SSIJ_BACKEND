package com.akeshya.dto.request;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private String categoryName;
    private String status;
    private Boolean isPublished;
    private List<String> colors = new ArrayList<>();
    private List<ProductSizeRequest> sizes = new ArrayList<>();
    private List<ProductImageRequest> images = new ArrayList<>();
    
}