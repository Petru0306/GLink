package com.greenlink.greenlink.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * DTO for Stripe checkout session information
 * Used for creating checkout sessions and processing payments
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeCheckoutDto {
    
    /**
     * The Stripe checkout session ID (starts with 'cs_')
     */
    private String sessionId;
    
    /**
     * The checkout session URL for the customer
     */
    private String checkoutUrl;
    
    /**
     * The connected account ID for the seller
     */
    private String connectedAccountId;
    
    /**
     * The line items in the checkout session
     */
    private List<CheckoutLineItem> lineItems;
    
    /**
     * The application fee amount in cents
     */
    private Long applicationFeeAmount;
    
    /**
     * The success URL for redirect after payment
     */
    private String successUrl;
    
    /**
     * The cancel URL for redirect if payment is cancelled
     */
    private String cancelUrl;
    
    /**
     * The payment mode (e.g., 'payment', 'subscription')
     */
    private String mode;
    
    /**
     * The currency code (e.g., 'usd', 'eur', 'cad')
     */
    private String currency;
    
    /**
     * The customer email (optional)
     */
    private String customerEmail;
    
    /**
     * Inner class for line items
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckoutLineItem {
        
        /**
         * The Stripe price ID
         */
        private String priceId;
        
        /**
         * The quantity of the item
         */
        private Integer quantity;
        
        /**
         * The product name (for display purposes)
         */
        private String name;
        
        /**
         * The product description (for display purposes)
         */
        private String description;
    }
} 