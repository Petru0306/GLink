package com.greenlink.greenlink.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class StripeConfig {
    
    @Value("${stripe.secret-key}")
    private String secretKey;
    
    @Value("${stripe.publishable-key}")
    private String publishableKey;
    
    @Value("${stripe.platform-commission-percentage:5.0}")
    private Double platformCommissionPercentage;
    
    @PostConstruct
    public void initStripe() {
        // Set the Stripe API key
        Stripe.apiKey = secretKey;
        
        // Validate that the API key is set
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalStateException("Stripe secret key is not configured. Please set STRIPE_SECRET_KEY environment variable.");
        }
        
        // Validate that the publishable key is set
        if (publishableKey == null || publishableKey.trim().isEmpty()) {
            throw new IllegalStateException("Stripe publishable key is not configured. Please set STRIPE_PUBLISHABLE_KEY environment variable.");
        }
    }
    
    public String getSecretKey() {
        return secretKey;
    }
    
    public String getPublishableKey() {
        return publishableKey;
    }
    
    public Double getPlatformCommissionPercentage() {
        return platformCommissionPercentage;
    }
} 