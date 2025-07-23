package com.greenlink.greenlink.dto;

import com.greenlink.greenlink.model.Product.Category;
import com.greenlink.greenlink.model.Product.Branch;
import java.time.LocalDateTime;
import java.util.Objects;

public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private double price;
    private Category category;
    private String imageUrl;
    private boolean ecoFriendly;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int stock;
    private Long sellerId;
    private String sellerName;
    private Integer sellerLevel;
    private Branch branch;
    private Double negotiatedPrice; // Price negotiated for the current user
    
    // Sale-related fields
    private boolean sold;
    private Long buyerId;
    private String buyerName;
    private LocalDateTime soldAt;

    // Constructori
    public ProductDto() {
        this.branch = Branch.VERDE; // Set default branch
    }

    public ProductDto(Long id, String name, String description, double price,
                      Category category, String imageUrl, boolean ecoFriendly,
                      LocalDateTime createdAt, LocalDateTime updatedAt, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.ecoFriendly = ecoFriendly;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.stock = stock;
    }
    
    public ProductDto(Long id, String name, String description, double price,
                      Category category, String imageUrl, boolean ecoFriendly,
                      LocalDateTime createdAt, LocalDateTime updatedAt, int stock,
                      Long sellerId, String sellerName, Integer sellerLevel, Branch branch) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.ecoFriendly = ecoFriendly;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.stock = stock;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.sellerLevel = sellerLevel;
        this.branch = branch;
    }
    
    public ProductDto(Long id, String name, String description, double price,
                      Category category, String imageUrl, boolean ecoFriendly,
                      LocalDateTime createdAt, LocalDateTime updatedAt, int stock,
                      Long sellerId, String sellerName, Integer sellerLevel, Branch branch, Double negotiatedPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.ecoFriendly = ecoFriendly;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.stock = stock;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.sellerLevel = sellerLevel;
        this.branch = branch;
        this.negotiatedPrice = negotiatedPrice;
    }

    // Getteri și Setteri (la fel ca în Product)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isEcoFriendly() {
        return ecoFriendly;
    }

    public void setEcoFriendly(boolean ecoFriendly) {
        this.ecoFriendly = ecoFriendly;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
    
    public Long getSellerId() {
        return sellerId;
    }
    
    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
    
    public String getSellerName() {
        return sellerName;
    }
    
    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }
    
    public Integer getSellerLevel() {
        return sellerLevel;
    }
    
    public void setSellerLevel(Integer sellerLevel) {
        this.sellerLevel = sellerLevel;
    }
    
    public Branch getBranch() {
        return branch;
    }
    
    public void setBranch(Branch branch) {
        this.branch = branch;
    }
    
    public Double getNegotiatedPrice() {
        return negotiatedPrice;
    }
    
    public void setNegotiatedPrice(Double negotiatedPrice) {
        this.negotiatedPrice = negotiatedPrice;
    }
    
    // Sale-related getters and setters
    public boolean isSold() {
        return sold;
    }
    
    public void setSold(boolean sold) {
        this.sold = sold;
    }
    
    public Long getBuyerId() {
        return buyerId;
    }
    
    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }
    
    public String getBuyerName() {
        return buyerName;
    }
    
    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }
    
    public LocalDateTime getSoldAt() {
        return soldAt;
    }
    
    public void setSoldAt(LocalDateTime soldAt) {
        this.soldAt = soldAt;
    }
    
    // Helper method to check if there's a negotiated price
    public boolean hasNegotiatedPrice() {
        return negotiatedPrice != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        
        // Handle comparison with both ProductDto and Product
        if (o instanceof ProductDto) {
            ProductDto that = (ProductDto) o;
            return Objects.equals(id, that.id);
        } else if (o.getClass().getSimpleName().equals("Product")) {
            // Compare with Product entity using reflection to avoid direct dependency
            try {
                Long thatId = (Long) o.getClass().getMethod("getId").invoke(o);
                return Objects.equals(id, thatId);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProductDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", price=" + price +
                ", sellerId=" + sellerId +
                '}';
    }
}