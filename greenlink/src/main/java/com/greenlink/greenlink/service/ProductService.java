package com.greenlink.greenlink.service;

import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.Product.Category;
import com.greenlink.greenlink.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private ProductDto convertToDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getImageUrl(),
                product.isEcoFriendly(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getStock()
        );
    }

    private Product convertToEntity(ProductDto dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        product.setEcoFriendly(dto.isEcoFriendly());
        product.setStock(dto.getStock());

        // Setăm timestamp-urile doar pentru produse noi
        if (dto.getId() == null) {
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
        }

        return product;
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<ProductDto> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));
        return convertToDto(product);
    }

    public ProductDto addProduct(ProductDto productDto) {
        Product product = convertToEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));

        // Actualizăm câmpurile
        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setCategory(productDto.getCategory());
        existingProduct.setEcoFriendly(productDto.isEcoFriendly());
        existingProduct.setStock(productDto.getStock());

        // Actualizăm imaginea doar dacă e furnizată una nouă
        if (productDto.getImageUrl() != null && !productDto.getImageUrl().equals(existingProduct.getImageUrl())) {
            // Ștergem imaginea veche dacă există
            if (existingProduct.getImageUrl() != null) {
                String oldFileName = existingProduct.getImageUrl().substring(
                        existingProduct.getImageUrl().lastIndexOf("/") + 1
                );
                fileStorageService.deleteFile(oldFileName);
            }
            existingProduct.setImageUrl(productDto.getImageUrl());
        }

        existingProduct.setUpdatedAt(LocalDateTime.now());
        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));

        // Ștergem imaginea asociată dacă există
        if (product.getImageUrl() != null) {
            String fileName = product.getImageUrl().substring(
                    product.getImageUrl().lastIndexOf("/") + 1
            );
            fileStorageService.deleteFile(fileName);
        }

        productRepository.deleteById(id);
    }

    public Page<ProductDto> getProductsByCategory(Category category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable)
                .map(this::convertToDto);
    }

    public Page<ProductDto> getEcoFriendlyProducts(Pageable pageable) {
        return productRepository.findByEcoFriendlyTrue(pageable)
                .map(this::convertToDto);
    }

    public Page<ProductDto> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        keyword, keyword, pageable)
                .map(this::convertToDto);
    }

    public Page<ProductDto> getFilteredProducts(
            Category category,
            Boolean ecoFriendly,
            String search,
            Double minPrice,
            Double maxPrice,
            Pageable pageable) {

        // If we only have a search term, use the existing search method
        if (search != null && !search.trim().isEmpty() &&
            category == null && ecoFriendly == null && minPrice == null && maxPrice == null) {
            return searchProducts(search.trim(), pageable);
        }

        // Create a specification for dynamic filtering
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            
            if (ecoFriendly != null) {
                predicates.add(cb.equal(root.get("ecoFriendly"), ecoFriendly));
            }
            
            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), searchTerm),
                    cb.like(cb.lower(root.get("description")), searchTerm)
                ));
            }
            
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable).map(this::convertToDto);
    }

    public List<ProductDto> filterProducts(Category category, Double minPrice, Double maxPrice, Boolean ecoFriendly) {
        return productRepository.findByFilters(category, minPrice, maxPrice, ecoFriendly)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProductDto> getSimilarProducts(Long productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));

        // Get products from the same category, excluding the current product
        return productRepository.findByCategoryAndIdNot(
                product.getCategory(), 
                productId, 
                PageRequest.of(0, limit)
            )
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
}