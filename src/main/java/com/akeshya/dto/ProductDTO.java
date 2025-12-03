package com.akeshya.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private String name;
    private String categoryName;
    private String subCategoryName;
    private List<ProductImageDTO> images;
    private List<String> colors;
    private List<ProductSizeDTO> sizes;
    private String status;
    private Boolean isPublished;
}