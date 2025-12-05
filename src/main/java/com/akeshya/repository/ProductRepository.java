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
public interface ProductRepository extends JpaRepository<Product, Long>{
    
    List<Product> findByIsPublishedTrue();
    
    // FIXED: Use JPQL query to access category through relationship
    @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName")
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);
    
    List<Product> findByStatus(String status);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
    List<Product> findByNameContaining(@Param("name") String name);
    
    // FIXED: Use JOIN to access category name through relationship
    @Query("SELECT DISTINCT c.name FROM Product p JOIN p.category c")
    List<String> findAllDistinctCategories();
    
    Optional<Product> findByIdAndIsPublishedTrue(Long id);

	List<Product> findByCategoryIdAndIsPublishedTrue(Long categoryId);
}