package com.akeshya.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
	private CategoryResponse category;
    private String status;
    private Boolean isPublished;
    private List<String> colors = new ArrayList<>();
    private List<ProductSizeResponse> sizes = new ArrayList<>();
    private List<ProductImageResponse> images = new ArrayList<>();
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}