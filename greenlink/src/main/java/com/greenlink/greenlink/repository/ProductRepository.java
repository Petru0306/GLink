package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.Product.Category;
import com.greenlink.greenlink.model.Product.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.sold = false")
    Page<Product> findByCategory(@Param("category") Category category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.ecoFriendly = true AND p.sold = false")
    Page<Product> findByEcoFriendlyTrue(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.sold = false AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            @Param("name") String name, @Param("description") String description, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "p.sold = false AND " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:ecoFriendly IS NULL OR p.ecoFriendly = :ecoFriendly)")
    List<Product> findByFilters(
            @Param("category") Category category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("ecoFriendly") Boolean ecoFriendly);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.id != :productId ORDER BY p.createdAt DESC")
    List<Product> findByCategoryAndIdNot(
            @Param("category") Category category,
            @Param("productId") Long productId,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.branch = :branch AND p.id != :productId ORDER BY p.createdAt DESC")
    List<Product> findByCategoryAndBranchAndIdNot(
            @Param("category") Category category,
            @Param("branch") Branch branch,
            @Param("productId") Long productId,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.branch = :branch AND p.id != :productId ORDER BY p.createdAt DESC")
    List<Product> findByBranchAndIdNot(
            @Param("branch") Branch branch,
            @Param("productId") Long productId,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId")
    List<Product> findBySellerId(@Param("sellerId") Long sellerId);

    long countBySellerId(Long sellerId);
    
    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.sold = false")
    List<Product> findAvailableProductsBySellerId(@Param("sellerId") Long sellerId);
    
    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.sold = true ORDER BY p.soldAt DESC")
    List<Product> findSoldProductsBySellerId(@Param("sellerId") Long sellerId);
    
    @Query("SELECT p FROM Product p WHERE p.sold = false")
    List<Product> findAllAvailableProducts();
    
    @Query("SELECT p FROM Product p WHERE p.buyer.id = :buyerId AND p.sold = true ORDER BY p.soldAt DESC")
    List<Product> findProductsByBuyerId(@Param("buyerId") Long buyerId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.buyer.id = ?1 AND p.sold = true")
    long countByBuyerId(Long buyerId);
}