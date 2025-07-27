package com.greenlink.greenlink.dto;

import com.greenlink.greenlink.model.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseDto {
    private Long id;
    private ProductDto product;
    private UserDto seller;
    private UserDto buyer;
    private int quantity;
    private BigDecimal totalPrice;
    private LocalDateTime purchaseDate;
    private boolean hasReview;
    
    // Constructors
    public PurchaseDto() {}
    
    public PurchaseDto(Long id, ProductDto product, UserDto seller, UserDto buyer, 
                      int quantity, BigDecimal totalPrice, LocalDateTime purchaseDate, boolean hasReview) {
        this.id = id;
        this.product = product;
        this.seller = seller;
        this.buyer = buyer;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.purchaseDate = purchaseDate;
        this.hasReview = hasReview;
    }
    
    // Static factory method to create from Product entity
    public static PurchaseDto fromProduct(Product product) {
        if (product.getBuyer() == null) {
            System.out.println("WARNING: Product " + product.getId() + " does not have a buyer set");
            return null; // Return null instead of throwing exception
        }
        
        // Create ProductDto
        ProductDto productDto = new ProductDto(
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
            product.getSeller() != null ? product.getSeller().getId() : null,
            product.getSeller() != null ? 
                product.getSeller().getFirstName() + " " + product.getSeller().getLastName() : null,
            product.getSeller() != null ? product.getSeller().getLevel() : null,
            product.getBranch()
        );
        
        // Create UserDto for seller
        UserDto sellerDto = null;
        if (product.getSeller() != null) {
            sellerDto = new UserDto();
            sellerDto.setId(product.getSeller().getId());
            sellerDto.setFirstName(product.getSeller().getFirstName());
            sellerDto.setLastName(product.getSeller().getLastName());
            sellerDto.setEmail(product.getSeller().getEmail());
        }
        
        // Create UserDto for buyer
        UserDto buyerDto = new UserDto();
        buyerDto.setId(product.getBuyer().getId());
        buyerDto.setFirstName(product.getBuyer().getFirstName());
        buyerDto.setLastName(product.getBuyer().getLastName());
        buyerDto.setEmail(product.getBuyer().getEmail());
        
        // Get the price to use (negotiated price if available, otherwise original price)
        double priceToUse = product.getPrice();
        Double negotiatedPrice = product.getNegotiatedPriceForUser(product.getBuyer().getId());
        if (negotiatedPrice != null) {
            priceToUse = negotiatedPrice;
        }
        
        return new PurchaseDto(
            product.getId(),
            productDto,
            sellerDto,
            buyerDto,
            1, // Default quantity is 1 for now
            BigDecimal.valueOf(priceToUse),
            product.getSoldAt(),
            false 
        );
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ProductDto getProduct() {
        return product;
    }
    
    public void setProduct(ProductDto product) {
        this.product = product;
    }
    
    public UserDto getSeller() {
        return seller;
    }
    
    public void setSeller(UserDto seller) {
        this.seller = seller;
    }
    
    public UserDto getBuyer() {
        return buyer;
    }
    
    public void setBuyer(UserDto buyer) {
        this.buyer = buyer;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }
    
    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
    
    public boolean isHasReview() {
        return hasReview;
    }
    
    public void setHasReview(boolean hasReview) {
        this.hasReview = hasReview;
    }
} 