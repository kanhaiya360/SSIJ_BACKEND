package com.akeshya.service;

import com.akeshya.dto.request.CategoryRequest;
import com.akeshya.dto.response.CategoryResponse;

import java.util.List;

import org.jspecify.annotations.Nullable;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);

	CategoryResponse updateCategoryStatus(Long id, boolean status);
}
