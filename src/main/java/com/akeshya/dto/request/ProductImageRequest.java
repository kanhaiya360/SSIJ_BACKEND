package com.akeshya.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public
class ProductImageRequest {
    private String imagePath;
    private Integer imageOrder;
    private Boolean isPrimary;
    private String altText;
}