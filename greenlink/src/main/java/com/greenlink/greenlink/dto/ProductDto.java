package com.greenlink.greenlink.dto;

import com.greenlink.greenlink.model.Product.Category;
import java.time.LocalDateTime;

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

    // Constructori
    public ProductDto() {}

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
}