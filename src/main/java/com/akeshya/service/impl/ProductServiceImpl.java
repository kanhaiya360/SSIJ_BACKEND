package com.akeshya.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.akeshya.dto.request.ProductImageRequest;
import com.akeshya.dto.request.ProductRequest;
import com.akeshya.dto.response.CategoryResponse;
import com.akeshya.dto.response.CategoryWithProductsResponse;
import com.akeshya.dto.response.ProductImageResponse;
import com.akeshya.dto.response.ProductResponse;
import com.akeshya.dto.response.ProductSizeResponse;
import com.akeshya.entity.Category;
import com.akeshya.entity.Product;
import com.akeshya.entity.ProductImage;
import com.akeshya.entity.ProductSize;
import com.akeshya.repository.CategoryRepository;
import com.akeshya.repository.ProductRepository;
import com.akeshya.service.FileStorageService;
import com.akeshya.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;
    private final CategoryRepository categoryRepository; 
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ResponseEntity<?> createProduct(ProductRequest request) {
        try {
            log.info("Creating new product: {}", request.getName());
            
            validateProductRequest(request);
            
            Product product = buildProductFromRequest(request);
            Product savedProduct = productRepository.save(product);
            
            log.info("Product created successfully with ID: {}", savedProduct.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(savedProduct));
            
        } catch (IllegalArgumentException e) {
            log.warn("Validation error while creating product: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating product: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getProductById(Long id) {
        try {
            log.info("Fetching product with ID: {}", id);
            
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            
            return ResponseEntity.ok(mapToResponse(product));
            
        } catch (RuntimeException e) {
            log.warn("Product not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving product with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving product: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getAllProducts() {
        try {
            log.info("Fetching all products");
            
            List<Product> products = productRepository.findAll();
            List<ProductResponse> response = products.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            
            log.info("Found {} products", response.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving all products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving products: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getPublishedProducts() {
        try {
            log.info("Fetching published products");
            
            List<Product> products = productRepository.findByIsPublishedTrue();
            List<ProductResponse> response = products.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            
            log.info("Found {} published products", response.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving published products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving published products: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getProductsByCategory(String categoryName) {
        try {
            log.info("Fetching products by category: {}", categoryName);
            
            List<Product> products = productRepository.findByCategoryName(categoryName);
            List<ProductResponse> response = products.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            
            log.info("Found {} products in category: {}", response.size(), categoryName);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving products by category {}: {}", categoryName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving products by category: " + e.getMessage());
        }
    }


    @Override
    public ResponseEntity<?> searchProductsByName(String name) {
        try {
            log.info("Searching products by name: {}", name);
            
            List<Product> products = productRepository.findByNameContaining(name);
            List<ProductResponse> response = products.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            
            log.info("Found {} products matching search: {}", response.size(), name);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching products by name {}: {}", name, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching products: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateProduct(Long id, ProductRequest request) {
        try {
            log.info("Updating product with ID: {}", id);
            
            validateProductRequest(request);
            
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            
            updateProductFromRequest(product, request);
            Product updatedProduct = productRepository.save(product);
            
            log.info("Product updated successfully with ID: {}", updatedProduct.getId());
            return ResponseEntity.ok(mapToResponse(updatedProduct));
            
        } catch (RuntimeException e) {
            log.warn("Product not found for update with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating product with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating product: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateProductStatus(Long id, Boolean isPublished) {
        try {
            log.info("Updating product publish status for ID: {} to {}", id, isPublished);
            
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            
            product.setIsPublished(isPublished);
            Product updatedProduct = productRepository.save(product);
            
            log.info("Product publish status updated successfully for ID: {}", id);
            return ResponseEntity.ok(mapToResponse(updatedProduct));
            
        } catch (RuntimeException e) {
            log.warn("Product not found for status update with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating product status with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating product status: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateProductPublishStatus(Long id, Boolean isPublished) {
        return updateProductStatus(id, isPublished);
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteProduct(Long id) {
        try {
            log.info("Deleting product with ID: {}", id);
            
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            
            productRepository.delete(product);
            
            log.info("Product deleted successfully with ID: {}", id);
            return ResponseEntity.ok("Product deleted successfully");
            
        } catch (RuntimeException e) {
            log.warn("Product not found for deletion with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting product with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting product: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getCategories() {
        try {
            log.info("Fetching all distinct categories");
            
            List<String> categories = productRepository.findAllDistinctCategories();
            
            log.info("Found {} distinct categories", categories.size());
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            log.error("Error retrieving categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving categories: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> toggleProductPublishStatus(Long id) {
        try {
            log.info("Toggling product publish status for ID: {}", id);
            
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            
            product.setIsPublished(!product.getIsPublished());
            Product updatedProduct = productRepository.save(product);
            
            log.info("Product publish status toggled to {} for ID: {}", updatedProduct.getIsPublished(), id);
            return ResponseEntity.ok(mapToResponse(updatedProduct));
            
        } catch (RuntimeException e) {
            log.warn("Product not found for toggle status with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error toggling product status with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error toggling product status: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getProductCountByStatus() {
        try {
            log.info("Fetching product count by status");
            
            List<Product> allProducts = productRepository.findAll();
            
            Map<String, Long> countByStatus = allProducts.stream()
                    .collect(Collectors.groupingBy(Product::getStatus, Collectors.counting()));
            
            Map<String, Object> response = new HashMap<>();
            response.put("publishedCount", allProducts.stream().filter(Product::getIsPublished).count());
            response.put("unpublishedCount", allProducts.stream().filter(p -> !p.getIsPublished()).count());
            response.put("statusWiseCount", countByStatus);
            response.put("totalCount", (long) allProducts.size());
            
            log.info("Product count statistics generated");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving product count by status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving product statistics: " + e.getMessage());
        }
    }

    // Private helper methods
    private void validateProductRequest(ProductRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID is required");
        }
    }

    private Product buildProductFromRequest(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));
        
        Product product = Product.builder()
                .name(request.getName().trim())
                .category(category)
                .status(request.getStatus() != null ? request.getStatus() : "Full")
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : false)
                .build();

        // Add colors
        if (request.getColors() != null) {
            product.getColors().addAll(request.getColors().stream()
                    .map(String::trim)
                    .filter(color -> !color.isEmpty())
                    .collect(Collectors.toList()));
        }

        // Add sizes
        if (request.getSizes() != null) {
            request.getSizes().forEach(sizeRequest -> {
                ProductSize size = ProductSize.builder()
                        .size(sizeRequest.getSizeValue())
                        .weight(sizeRequest.getWeight())
                        .build();
                product.addSize(size);
            });
        }

        // Add images
        if (request.getImages() != null) {
            request.getImages().forEach(imageRequest -> {
                ProductImage image = ProductImage.builder()
                        .imagePath(imageRequest.getImagePath())
                        .imageOrder(imageRequest.getImageOrder() != null ? imageRequest.getImageOrder() : 0)
                        .isPrimary(imageRequest.getIsPrimary() != null ? imageRequest.getIsPrimary() : false)
                        .altText(imageRequest.getAltText())
                        .build();
                product.addImage(image);
            });
        }

        return product;
    }

    private void updateProductFromRequest(Product product, ProductRequest request) {
        product.setName(request.getName().trim());
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        if (request.getIsPublished() != null) {
            product.setIsPublished(request.getIsPublished());
        }

        // Update colors
        product.getColors().clear();
        if (request.getColors() != null) {
            product.getColors().addAll(request.getColors().stream()
                    .map(String::trim)
                    .filter(color -> !color.isEmpty())
                    .collect(Collectors.toList()));
        }

        // Update sizes
        product.getSizes().clear();
        if (request.getSizes() != null) {
            request.getSizes().forEach(sizeRequest -> {
                ProductSize size = ProductSize.builder()
                        .size(sizeRequest.getSizeValue())
                        .weight(sizeRequest.getWeight())
                        .build();
                product.addSize(size);
            });
        }

        // Update images
        product.getImages().clear();
        if (request.getImages() != null) {
            request.getImages().forEach(imageRequest -> {
                ProductImage image = ProductImage.builder()
                        .imagePath(imageRequest.getImagePath())
                        .imageOrder(imageRequest.getImageOrder() != null ? imageRequest.getImageOrder() : 0)
                        .isPrimary(imageRequest.getIsPrimary() != null ? imageRequest.getIsPrimary() : false)
                        .altText(imageRequest.getAltText())
                        .build();
                product.addImage(image);
            });
        }
    }

    private ProductResponse mapToResponse(Product product) {
        // Map category to CategoryResponse
        CategoryResponse categoryResponse = null;
        if (product.getCategory() != null) {
            categoryResponse = new CategoryResponse();
            categoryResponse.setId(product.getCategory().getId());
            categoryResponse.setName(product.getCategory().getName());
            categoryResponse.setDescription(product.getCategory().getDescription());
            categoryResponse.setStatus(product.getCategory().isStatus());
            categoryResponse.setCreatedAt(product.getCategory().getCreatedAt().toString());
            categoryResponse.setUpdatedAt(product.getCategory().getUpdatedAt().toString());
        }
        
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(categoryResponse) 
                .status(product.getStatus())
                .isPublished(product.getIsPublished())
                .colors(new ArrayList<>(product.getColors()))
                .sizes(product.getSizes().stream().map(size -> ProductSizeResponse.builder()
                        .id(size.getId())
                        .sizeValue(size.getSize())
                        .weight(size.getWeight())
                        .build()).collect(Collectors.toList()))
                .images(product.getImages().stream().map(image -> ProductImageResponse.builder()
                        .id(image.getId())
                        .imagePath(image.getImagePath())
                        .imageOrder(image.getImageOrder())
                        .isPrimary(image.getIsPrimary())
                        .altText(image.getAltText())
                        .build()).collect(Collectors.toList()))
                .createdDate(product.getCreatedDate())
                .updatedDate(product.getUpdatedDate())
                .build();
    }
    
    @Override
    @Transactional
    public ResponseEntity<?> createProductWithImages(String productData, MultipartFile[] images) {
        try {
            log.info("Creating product with images");
            
            // Parse product data from JSON string
            ProductRequest request = objectMapper.readValue(productData, ProductRequest.class);
            
            // Validate product request
            validateProductRequest(request);
            
            // Handle image uploads
            if (images != null && images.length > 0) {
                processImageUploads(request, images);
            }
            
            // Create product
            Product product = buildProductFromRequest(request);
            Product savedProduct = productRepository.save(product);
            
            log.info("Product created successfully with ID: {} and {} images", 
                    savedProduct.getId(), savedProduct.getImages().size());
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(savedProduct));
            
        } catch (Exception e) {
            log.error("Error creating product with images: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating product: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> createProduct(ProductRequest productRequest, MultipartFile[] imageFiles) {
        try {
            log.info("Creating product with separate file upload");
            
            validateProductRequest(productRequest);
            
            // Handle image uploads
            if (imageFiles != null && imageFiles.length > 0) {
                processImageUploads(productRequest, imageFiles);
            }
            
            Product product = buildProductFromRequest(productRequest);
            Product savedProduct = productRepository.save(product);
            
            log.info("Product created successfully with ID: {} and {} images", 
                    savedProduct.getId(), savedProduct.getImages().size());
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(savedProduct));
            
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating product: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateProductWithImages(Long id, ProductRequest request, MultipartFile[] imageFiles) {
        try {
            log.info("Updating product with ID: {} and {} new images", id, 
                    imageFiles != null ? imageFiles.length : 0);
            
            validateProductRequest(request);
            
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            
            // Handle new image uploads
            if (imageFiles != null && imageFiles.length > 0) {
                processImageUploads(request, imageFiles);
            }
            
            updateProductFromRequest(product, request);
            Product updatedProduct = productRepository.save(product);
            
            log.info("Product updated successfully with ID: {}", updatedProduct.getId());
            return ResponseEntity.ok(mapToResponse(updatedProduct));
            
        } catch (RuntimeException e) {
            log.warn("Product not found for update with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating product with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating product: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> addImagesToProduct(Long productId, MultipartFile[] imageFiles) {
        try {
            log.info("Adding {} images to product ID: {}", 
                    imageFiles != null ? imageFiles.length : 0, productId);
            
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
            
            if (imageFiles != null && imageFiles.length > 0) {
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        try {
                            String imageUrl = fileStorageService.storeFile(file);
                            ProductImage image = ProductImage.builder()
                                    .imagePath(imageUrl)
                                    .imageOrder(product.getImages().size())
                                    .isPrimary(false) // New images are not primary by default
                                    .altText("Product image")
                                    .build();
                            product.addImage(image);
                        } catch (Exception e) {
                            log.warn("Failed to upload image: {}", e.getMessage());
                        }
                    }
                }
                
                Product updatedProduct = productRepository.save(product);
                log.info("Added {} images to product ID: {}", 
                        imageFiles.length, productId);
                return ResponseEntity.ok(mapToResponse(updatedProduct));
            } else {
                return ResponseEntity.badRequest().body("No images provided");
            }
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error adding images to product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding images: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> removeImageFromProduct(Long productId, Long imageId) {
        try {
            log.info("Removing image {} from product ID: {}", imageId, productId);
            
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
            
            ProductImage imageToRemove = product.getImages().stream()
                    .filter(img -> img.getId().equals(imageId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
            
            // Delete the physical file
            fileStorageService.deleteFile(imageToRemove.getImagePath());
            
            // Remove from product
            product.getImages().remove(imageToRemove);
            
            // If we removed the primary image and there are other images, set a new primary
            if (imageToRemove.getIsPrimary() && !product.getImages().isEmpty()) {
                product.getImages().get(0).setIsPrimary(true);
            }
            
            Product updatedProduct = productRepository.save(product);
            
            log.info("Image removed successfully from product ID: {}", productId);
            return ResponseEntity.ok(mapToResponse(updatedProduct));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error removing image from product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error removing image: " + e.getMessage());
        }
    }

    /**
     * Process image uploads and add them to the product request
     */
    private void processImageUploads(ProductRequest request, MultipartFile[] imageFiles) {
        if (request.getImages() == null) {
            request.setImages(new ArrayList<>());
        }
        
        int startOrder = request.getImages().size();
        
        for (int i = 0; i < imageFiles.length; i++) {
            MultipartFile file = imageFiles[i];
            if (!file.isEmpty()) {
                try {
                    String imageUrl = fileStorageService.storeFile(file);
                    
                    ProductImageRequest imageRequest = new ProductImageRequest();
                    imageRequest.setImagePath(imageUrl);
                    imageRequest.setImageOrder(startOrder + i);
                    imageRequest.setIsPrimary(request.getImages().isEmpty() && i == 0); // First image is primary
                    imageRequest.setAltText(file.getOriginalFilename());
                    
                    request.getImages().add(imageRequest);
                    
                    log.debug("Uploaded image: {}", imageUrl);
                } catch (Exception e) {
                    log.warn("Failed to upload image {}: {}", file.getOriginalFilename(), e.getMessage());
                }
            }
        }
    }

    @Override
    public ResponseEntity<?> getProductsByCategoryId(Long categoryId) {
        try {
            log.info("Fetching products for category ID: {}", categoryId);
            
            // Validate category ID
            if (categoryId == null || categoryId <= 0) {
                String errorMsg = "Invalid category ID. Category ID must be a positive number.";
                log.warn(errorMsg);
                return ResponseEntity.badRequest().body(errorMsg);
            }
            
            // Check if category exists
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> {
                        String errorMsg = String.format("Category not found with ID: %d", categoryId);
                        log.warn(errorMsg);
                        return new RuntimeException(errorMsg);
                    });
            
            // Get published products for this category
            List<Product> products = productRepository.findByCategoryIdAndIsPublishedTrue(categoryId);
            
            log.info("Found {} published products for category: {} (ID: {})", 
                    products.size(), category.getName(), categoryId);
            
            // If no products found, you can return an empty list or a message
            if (products.isEmpty()) {
                log.info("No published products found for category: {} (ID: {})", 
                        category.getName(), categoryId);
                
                // You can choose to return an empty response or a message
                CategoryWithProductsResponse emptyResponse = new CategoryWithProductsResponse();
                emptyResponse.setCategory(mapToCategoryResponse(category));
                emptyResponse.setProducts(List.of()); // Empty list
                
                return ResponseEntity.ok(emptyResponse);
            }
            
            // Map category to CategoryResponse
            CategoryResponse categoryResponse = CategoryResponse.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .description(category.getDescription())
                    .status(category.isStatus())
                    .createdAt(category.getCreatedAt() != null ? 
                            category.getCreatedAt().toString() : null)
                    .updatedAt(category.getUpdatedAt() != null ? 
                            category.getUpdatedAt().toString() : null)
                    .build();
            
            List<ProductResponse> productResponses = products.stream()
                    .map(product -> {
                        // For each product, create response with category info
                        return ProductResponse.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .category(categoryResponse) // Add category to each product
                                .status(product.getStatus())
                                .isPublished(product.getIsPublished())
                                .colors(new ArrayList<>(product.getColors()))
                                .sizes(product.getSizes().stream()
                                        .map(size -> ProductSizeResponse.builder()
                                                .id(size.getId())
                                                .sizeValue(size.getSize())
                                                .weight(size.getWeight())
                                                .build())
                                        .collect(Collectors.toList()))
                                .images(product.getImages().stream()
                                        .map(image -> ProductImageResponse.builder()
                                                .id(image.getId())
                                                .imagePath(image.getImagePath())
                                                .imageOrder(image.getImageOrder())
                                                .isPrimary(image.getIsPrimary())
                                                .altText(image.getAltText())
                                                .build())
                                        .collect(Collectors.toList()))
                                .createdDate(product.getCreatedDate())
                                .updatedDate(product.getUpdatedDate())
                                .build();
                    })
                    .collect(Collectors.toList());
            
            // Create the final response
            CategoryWithProductsResponse response = new CategoryWithProductsResponse();
            response.setCategory(categoryResponse);
            response.setProducts(productResponses);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Category not found error for ID {}: {}", categoryId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            
        } catch (Exception e) {
            log.error("Error fetching products for category ID {}: {}", categoryId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching products. Please try again later.");
        }
    }
    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .status(category.isStatus())
                .createdAt(category.getCreatedAt() != null ? 
                        category.getCreatedAt().toString() : null)
                .updatedAt(category.getUpdatedAt() != null ? 
                        category.getUpdatedAt().toString() : null)
                .build();
    }
}