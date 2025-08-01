package com.greenlink.greenlink.service;

import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.dto.PurchaseDto;
import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.model.Product.Category;
import com.greenlink.greenlink.repository.ProductRepository;
import com.greenlink.greenlink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChallengeTrackingService challengeTrackingService;

    private ProductDto convertToDto(Product product) {
        String sellerName = product.getSeller() != null ? 
                product.getSeller().getFirstName() + " " + product.getSeller().getLastName() : null;
        Long sellerId = product.getSeller() != null ? product.getSeller().getId() : null;
        Integer sellerLevel = product.getSeller() != null ? product.getSeller().getLevel() : null;
        
        String buyerName = product.getBuyer() != null ? 
                product.getBuyer().getFirstName() + " " + product.getBuyer().getLastName() : null;
        Long buyerId = product.getBuyer() != null ? product.getBuyer().getId() : null;
        
        ProductDto dto = new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getImageUrl(),
                product.isEcoFriendly(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getStock(),
                sellerId,
                sellerName,
                sellerLevel,
                product.getBranch() != null ? product.getBranch() : Product.Branch.VERDE
        );
        
        dto.setSold(product.isSold());
        dto.setBuyerId(buyerId);
        dto.setBuyerName(buyerName);
        dto.setSoldAt(product.getSoldAt());
        
        return dto;
    }

    private ProductDto convertToDto(Product product, User currentUser) {
        String sellerName = product.getSeller() != null ? 
                product.getSeller().getFirstName() + " " + product.getSeller().getLastName() : null;
        Long sellerId = product.getSeller() != null ? product.getSeller().getId() : null;
        Integer sellerLevel = product.getSeller() != null ? product.getSeller().getLevel() : null;
        
        String buyerName = product.getBuyer() != null ? 
                product.getBuyer().getFirstName() + " " + product.getBuyer().getLastName() : null;
        Long buyerId = product.getBuyer() != null ? product.getBuyer().getId() : null;
        
        Double negotiatedPrice = null;
        if (currentUser != null) {
            negotiatedPrice = product.getNegotiatedPriceForUser(currentUser.getId());
        }
        
        ProductDto dto = new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getImageUrl(),
                product.isEcoFriendly(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getStock(),
                sellerId,
                sellerName,
                sellerLevel,
                product.getBranch() != null ? product.getBranch() : Product.Branch.VERDE,
                negotiatedPrice
        );
        
        dto.setSold(product.isSold());
        dto.setBuyerId(buyerId);
        dto.setBuyerName(buyerName);
        dto.setSoldAt(product.getSoldAt());
        
        return dto;
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
        
        if (dto.getBranch() != null) {
            product.setBranch(dto.getBranch());
        }

        if (dto.getSellerId() != null) {
            userRepository.findById(dto.getSellerId())
                    .ifPresent(product::setSeller);
        }

        if (dto.getId() == null) {
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
        }

        return product;
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAllAvailableProducts().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<ProductDto> getProducts(Pageable pageable) {
        return productRepository.findAllAvailableProducts().stream()
                .collect(Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> {
                        int start = (int) pageable.getOffset();
                        int end = Math.min((start + pageable.getPageSize()), list.size());
                        if (start > list.size()) {
                            return Page.empty(pageable);
                        }
                        return new org.springframework.data.domain.PageImpl<>(
                            list.subList(start, end).stream().map(this::convertToDto).collect(Collectors.toList()),
                            pageable,
                            list.size()
                        );
                    }
                ));
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));
        return convertToDto(product);
    }

    public ProductDto getProductById(Long id, User currentUser) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));
        return convertToDto(product, currentUser);
    }

    public ProductDto addProduct(ProductDto productDto) {
        Product product = convertToEntity(productDto);
        
        if (product.getSeller() == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
                product.setSeller((User) auth.getPrincipal());
            }
        }
        
        Product savedProduct = productRepository.save(product);
        ProductDto savedProductDto = convertToDto(savedProduct);
        
        if (savedProduct.getSeller() != null) {
            try {
                logger.info("Tracking marketplace item listing for user: {}", savedProduct.getSeller().getId());
                challengeTrackingService.trackUserAction(savedProduct.getSeller().getId(), "MARKETPLACE_ITEM_LISTED", savedProductDto);
            } catch (Exception e) {
                logger.error("Error tracking marketplace item listing: {}", e.getMessage(), e);
            }
        }
        
        return savedProductDto;
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));

        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setCategory(productDto.getCategory());
        existingProduct.setEcoFriendly(productDto.isEcoFriendly());
        existingProduct.setStock(productDto.getStock());
        
        if (productDto.getBranch() != null) {
            existingProduct.setBranch(productDto.getBranch());
        }

        if (productDto.getImageUrl() != null && !productDto.getImageUrl().equals(existingProduct.getImageUrl())) {
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

        List<User> usersWithFavorite = userRepository.findByFavoriteProductsContains(product);
        for (User user : usersWithFavorite) {
            user.getFavoriteProducts().remove(product);
            userRepository.save(user);
        }

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
            Product.Branch branch,
            Category category,
            Boolean ecoFriendly,
            String search,
            Double minPrice,
            Double maxPrice,
            Pageable pageable) {

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("sold"), false));
            logger.info("Filtering out sold products - only showing available products");

            if (branch != null) {
                predicates.add(cb.equal(root.get("branch"), branch));
            }
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

        Page<Product> products = productRepository.findAll(spec, pageable);
        logger.info("Found {} products in marketplace (filtered out sold products)", products.getTotalElements());
        
        for (Product product : products.getContent()) {
            logger.info("Product in marketplace: ID={}, Name={}, Sold={}", 
                       product.getId(), product.getName(), product.isSold());
        }
        
        return products.map(this::convertToDto);
    }

    public Page<ProductDto> getFilteredProducts(
            Category category,
            Boolean ecoFriendly,
            String search,
            Double minPrice,
            Double maxPrice,
            Pageable pageable) {
        return getFilteredProducts(null, category, ecoFriendly, search, minPrice, maxPrice, pageable);
    }

    public List<ProductDto> filterProducts(Category category, Double minPrice, Double maxPrice, Boolean ecoFriendly) {
        return productRepository.findByFilters(category, minPrice, maxPrice, ecoFriendly)
                .stream()
                .filter(product -> !product.isSold()) 
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProductDto> getSimilarProducts(Long productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));

        List<Product> similarProducts = productRepository.findByCategoryAndBranchAndIdNot(
                product.getCategory(),
                product.getBranch(),
                productId, 
                PageRequest.of(0, limit)
            );
            
        if (similarProducts.isEmpty()) {
            similarProducts = productRepository.findByBranchAndIdNot(
                product.getBranch(),
                productId,
                PageRequest.of(0, limit)
            );
        }
        
        return similarProducts.stream()
                .filter(p -> !p.isSold()) 
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ProductDto> getSimilarProducts(Long productId, int limit, User currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));

        List<Product> similarProducts = productRepository.findByCategoryAndBranchAndIdNot(
                product.getCategory(),
                product.getBranch(),
                productId, 
                PageRequest.of(0, limit)
            );
            
        if (similarProducts.isEmpty()) {
            similarProducts = productRepository.findByBranchAndIdNot(
                product.getBranch(),
                productId,
                PageRequest.of(0, limit)
            );
        }
        
        return similarProducts.stream()
                .filter(p -> !p.isSold()) 
                .map(p -> convertToDto(p, currentUser))
                .collect(Collectors.toList());
    }
    
    public List<ProductDto> getProductsBySeller(Long sellerId) {
        List<Product> products = productRepository.findAvailableProductsBySellerId(sellerId);
        
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ProductDto> getSoldProductsBySeller(Long sellerId) {
        System.out.println("=== GETTING SOLD PRODUCTS FOR SELLER ===");
        System.out.println("Seller ID: " + sellerId);
        
        List<Product> products = productRepository.findSoldProductsBySellerId(sellerId);
        
        System.out.println("Found " + products.size() + " sold products for seller " + sellerId);
        for (Product p : products) {
            System.out.println("  - Product: " + p.getName() + " (ID: " + p.getId() + ") - sold: " + p.isSold() + ", buyer: " + (p.getBuyer() != null ? p.getBuyer().getEmail() : "null"));
        }
        
        List<ProductDto> result = products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        System.out.println("Returning " + result.size() + " ProductDto objects");
        System.out.println("=== END GETTING SOLD PRODUCTS ===");
        
        return result;
    }
    
    public Page<ProductDto> getSoldProductsBySellerPaginated(Long sellerId, Pageable pageable) {
        List<Product> allSoldProducts = productRepository.findSoldProductsBySellerId(sellerId);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allSoldProducts.size());
        
        if (start > allSoldProducts.size()) {
            return Page.empty(pageable);
        }
        
        List<Product> pageContent = allSoldProducts.subList(start, end);
        List<ProductDto> dtoContent = pageContent.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
                dtoContent,
                pageable,
                allSoldProducts.size()
        );
    }
    
    public List<PurchaseDto> getBoughtProductsByBuyer(Long buyerId) {
        List<Product> products = productRepository.findProductsByBuyerId(buyerId);
        
        return products.stream()
                .map(PurchaseDto::fromProduct)
                .filter(purchaseDto -> purchaseDto != null)
                .collect(Collectors.toList());
    }
    
    public Page<PurchaseDto> getBoughtProductsByBuyerPaginated(Long buyerId, Pageable pageable) {
        List<Product> allBoughtProducts = productRepository.findProductsByBuyerId(buyerId);
        
        List<PurchaseDto> allPurchaseDtos = allBoughtProducts.stream()
                .map(PurchaseDto::fromProduct)
                .filter(purchaseDto -> purchaseDto != null)
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allPurchaseDtos.size());
        
        if (start > allPurchaseDtos.size()) {
            return Page.empty(pageable);
        }
        
        List<PurchaseDto> pageContent = allPurchaseDtos.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(
                pageContent,
                pageable,
                allPurchaseDtos.size()
        );
    }
    
    @Transactional
    public void setNegotiatedPrice(Long productId, Long userId, Double price) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));
        
        product.setNegotiatedPriceForUser(userId, price);
        productRepository.save(product);
    }
}