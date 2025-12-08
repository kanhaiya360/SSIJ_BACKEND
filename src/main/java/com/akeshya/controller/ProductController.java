package com.akeshya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.akeshya.dto.request.ProductRequest;
import com.akeshya.service.ProductService;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;
    // Public endpoints
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/published")
    public ResponseEntity<?> getPublishedProducts() {
        return productService.getPublishedProducts();
    }

    @GetMapping("/category/{categoryId}/products")
    public ResponseEntity<?> getProductsByCategoryId(@PathVariable Long categoryId) {
        return productService.getProductsByCategoryId(categoryId);
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable String categoryName) {
        return productService.getProductsByCategory(categoryName);
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return productService.getCategories();
    }

    // Admin only endpoints
    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(
            @RequestPart("productData") String productData,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        return productService.createProductWithImages(productData, images);
    }

    @PostMapping("/with-images")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProductWithImages(
            @RequestPart("productRequest") String productRequestStr,
            @RequestPart(value = "imageFiles", required = false) MultipartFile[] imageFiles) {
        
        try {
            ProductRequest productRequest = objectMapper.readValue(productRequestStr, ProductRequest.class);
            return productService.createProduct(productRequest, imageFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid product data: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestPart("productRequest") String productRequestStr,
            @RequestPart(value = "imageFiles", required = false) MultipartFile[] imageFiles) {
        
        try {
            ProductRequest productRequest = objectMapper.readValue(productRequestStr, ProductRequest.class);
            return productService.updateProductWithImages(id, productRequest, imageFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid product data: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }

    @PatchMapping("/{id}/publish/{isPublished}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProductStatus(@PathVariable Long id, @PathVariable Boolean isPublished) {
        return productService.updateProductStatus(id, isPublished);
    }

    @PostMapping("/{id}/images")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addProductImages(
            @PathVariable Long id,
            @RequestPart("imageFiles") MultipartFile[] imageFiles) {
        return productService.addImagesToProduct(id, imageFiles);
    }

    @DeleteMapping("/{productId}/images/{imageId}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        return productService.removeImageFromProduct(productId, imageId);
    }
}