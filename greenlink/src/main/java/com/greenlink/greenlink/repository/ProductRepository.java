package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.Product.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(Category category, Pageable pageable);

    Page<Product> findByEcoFriendlyTrue(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:ecoFriendly IS NULL OR p.ecoFriendly = :ecoFriendly)")
    List<Product> findByFilters(
            @Param("category") Category category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("ecoFriendly") Boolean ecoFriendly);
}