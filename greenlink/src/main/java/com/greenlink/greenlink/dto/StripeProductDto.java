package com.greenlink.greenlink.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for Stripe product information
 * Used for creating and managing products on connected accounts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeProductDto {
    
    /**
     * The Stripe product ID (starts with 'prod_')
     */
    private String productId;
    
    /**
     * The Stripe price ID (starts with 'price_')
     */
    private String priceId;
    
    /**
     * The name of the product
     */
    private String name;
    
    /**
     * The description of the product
     */
    private String description;
    
    /**
     * The price in cents (e.g., 1000 = $10.00)
     */
    private Long priceInCents;
    
    /**
     * The currency code (e.g., 'usd', 'eur', 'cad')
     */
    private String currency;
    
    /**
     * The connected account ID this product belongs to
     */
    private String connectedAccountId;
    
    /**
     * Whether the product is active
     */
    private Boolean active;
    
    /**
     * The product images (URLs)
     */
    private String[] images;
    
    /**
     * The product metadata
     */
    private java.util.Map<String, String> metadata;
} 