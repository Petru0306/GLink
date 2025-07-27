package com.greenlink.greenlink.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //UUID - de vazut.

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private double price;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String imageUrl;

    @Column(nullable = false)
    private boolean ecoFriendly;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private int stock;

    // În Product.java păstrăm doar calea:
    @Column
    private String imagePath;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Branch branch = Branch.VERDE;
    
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;
    
    // Sale-related fields
    @Column(nullable = false)
    private boolean sold = false;
    
    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;
    
    @Column(name = "sold_at")
    private LocalDateTime soldAt;
    
    // Link to delivery conversation - temporarily commented out until migration works
    // @ManyToOne
    // @JoinColumn(name = "conversation_id")
    // private Conversation deliveryConversation;
    
    // Map to store negotiated prices between users and this product
    @ElementCollection
    @CollectionTable(name = "negotiated_prices", 
                    joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "negotiated_price")
    private Map<Long, Double> negotiatedPrices = new HashMap<>();

    public enum Category {
        BIO("Produse Bio"),
        COSMETICS("Cosmetice Naturale"),
        RECYCLED("Produse Reciclate");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Branch { VERDE, FOOD, ELECTRO }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructori
    public Product() {}

    public Product(String name, String description, double price, Category category,
                   String imageUrl, boolean ecoFriendly, int stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.ecoFriendly = ecoFriendly;
        this.stock = stock;
    }

    // Getteri și Setteri
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
        // Return the imageUrl as is, since we're now using a dedicated controller
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
    
    public Branch getBranch() {
        return branch;
    }
    
    public void setBranch(Branch branch) {
        this.branch = branch;
    }
    
    public User getSeller() {
        return seller;
    }
    
    public void setSeller(User seller) {
        this.seller = seller;
    }
    
    public boolean isSold() {
        return sold;
    }
    
    public void setSold(boolean sold) {
        this.sold = sold;
    }
    
    public User getBuyer() {
        return buyer;
    }
    
    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }
    
    public LocalDateTime getSoldAt() {
        return soldAt;
    }
    
    public void setSoldAt(LocalDateTime soldAt) {
        this.soldAt = soldAt;
    }
    
    // Temporarily commented out until migration works
    /*
    public Conversation getDeliveryConversation() {
        return deliveryConversation;
    }
    
    public void setDeliveryConversation(Conversation deliveryConversation) {
        this.deliveryConversation = deliveryConversation;
    }
    */
    
    public Map<Long, Double> getNegotiatedPrices() {
        return negotiatedPrices;
    }
    
    public void setNegotiatedPrices(Map<Long, Double> negotiatedPrices) {
        this.negotiatedPrices = negotiatedPrices;
    }
    
    public Double getNegotiatedPriceForUser(Long userId) {
        return negotiatedPrices.get(userId);
    }
    
    public void setNegotiatedPriceForUser(Long userId, Double price) {
        negotiatedPrices.put(userId, price);
    }
}