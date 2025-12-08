package com.akeshya.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class CategoryWithProductsResponse {
	private CategoryResponse category;
	private List<ProductResponse> products;
}
