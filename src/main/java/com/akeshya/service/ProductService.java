package com.akeshya.service;

import com.akeshya.dto.request.ProductRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
    
    // Existing methods
    ResponseEntity<?> createProduct(ProductRequest request);
    ResponseEntity<?> getProductById(Long id);
    ResponseEntity<?> getAllProducts();
    ResponseEntity<?> getPublishedProducts();
    ResponseEntity<?> getProductsByCategory(String categoryName);
//    ResponseEntity<?> getProductsByCategoryAndSubCategory(String categoryName, String subCategoryName);
    ResponseEntity<?> searchProductsByName(String name);
    ResponseEntity<?> updateProduct(Long id, ProductRequest request);
    ResponseEntity<?> updateProductStatus(Long id, Boolean isPublished);
    ResponseEntity<?> updateProductPublishStatus(Long id, Boolean isPublished);
    ResponseEntity<?> deleteProduct(Long id);
    ResponseEntity<?> getCategories();

    ResponseEntity<?> toggleProductPublishStatus(Long id);
    ResponseEntity<?> getProductCountByStatus();

    // New methods for file upload
    ResponseEntity<?> createProductWithImages(String productData, MultipartFile[] images);
    ResponseEntity<?> createProduct(ProductRequest productRequest, MultipartFile[] imageFiles);
    ResponseEntity<?> updateProductWithImages(Long id, ProductRequest productRequest, MultipartFile[] imageFiles);
    ResponseEntity<?> addImagesToProduct(Long productId, MultipartFile[] imageFiles);
    ResponseEntity<?> removeImageFromProduct(Long productId, Long imageId);
//	Caterory with products
	ResponseEntity<?> getProductsByCategoryId(Long categoryId);
}