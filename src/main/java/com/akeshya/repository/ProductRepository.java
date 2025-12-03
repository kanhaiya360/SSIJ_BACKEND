package com.akeshya.repository;

import com.akeshya.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    
    List<Product> findByIsPublishedTrue();
    List<Product> findByCategoryName(String categoryName);
//    List<Product> findByCategoryNameAndSubCategoryName(String categoryName, String subCategoryName);
    List<Product> findByStatus(String status);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
    List<Product> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT DISTINCT p.categoryName FROM Product p")
    List<String> findAllDistinctCategories();
    
    Optional<Product> findByIdAndIsPublishedTrue(Long id);
}