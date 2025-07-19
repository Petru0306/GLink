package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.ProductRepository;
import com.greenlink.greenlink.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {
    
    @Autowired
    private StripeService stripeService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Process a successful payment and update product state
     */
    @Transactional
    public void processSuccessfulPayment(String sessionId) throws StripeException {
        Session session = stripeService.retrieveSession(sessionId);
        
        // Extract metadata
        String productIdStr = session.getMetadata().get("product_id");
        String buyerIdStr = session.getMetadata().get("buyer_id");
        
        if (productIdStr == null || buyerIdStr == null) {
            throw new RuntimeException("Missing required metadata in session");
        }
        
        Long productId = Long.parseLong(productIdStr);
        Long buyerId = Long.parseLong(buyerIdStr);
        
        // Get product and buyer
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));
        
        // Check if product is still available
        if (product.isSold()) {
            throw new RuntimeException("Product is already sold");
        }
        
        // Update product state
        product.setSold(true);
        product.setBuyer(buyer);
        product.setSoldAt(LocalDateTime.now());
        
        productRepository.save(product);
    }
    
    /**
     * Create a checkout session for a product
     */
    public String createCheckoutSession(Long productId, String successUrl, String cancelUrl) throws StripeException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.isSold()) {
            throw new RuntimeException("Product is already sold");
        }
        
        // Get or create buyer (current user)
        User buyer = getCurrentUser();
        
        // Ensure buyer has a Stripe customer ID
        if (buyer.getStripeCustomerId() == null) {
            stripeService.createCustomer(buyer);
            userRepository.save(buyer);
        }
        
        // Create checkout session
        Session session = stripeService.createCheckoutSession(product, buyer, successUrl, cancelUrl);
        
        return session.getUrl();
    }
    
    /**
     * Get current user (this should be implemented based on your authentication)
     */
    private User getCurrentUser() {
        // This should be implemented to get the current authenticated user
        // For now, we'll throw an exception - this should be injected from the controller
        throw new RuntimeException("Current user should be passed from controller");
    }
    
    /**
     * Get current user with dependency injection
     */
    public String createCheckoutSession(Long productId, User currentUser, String successUrl, String cancelUrl) throws StripeException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.isSold()) {
            throw new RuntimeException("Product is already sold");
        }
        
        // Ensure buyer has a Stripe customer ID
        if (currentUser.getStripeCustomerId() == null) {
            stripeService.createCustomer(currentUser);
            userRepository.save(currentUser);
        }
        
        // TEMPORARILY DISABLED: Skip seller Stripe account check
        // TODO: Re-enable this check after Stripe Connect is properly configured
        /*
        // Ensure seller has a Stripe account
        if (product.getSeller().getStripeAccountId() == null) {
            throw new RuntimeException("Seller must complete Stripe onboarding before accepting payments");
        }
        */
        
        // Create checkout session
        Session session = stripeService.createCheckoutSession(product, currentUser, successUrl, cancelUrl);
        
        return session.getUrl();
    }
    
    /**
     * Process admin purchase - mark product as sold without Stripe payment
     */
    @Transactional
    public void processAdminPurchase(Product product, User buyer) {
        // Check if product is still available
        if (product.isSold()) {
            throw new RuntimeException("Product is already sold");
        }
        
        // Check if buyer is not the seller
        if (product.getSeller().getId().equals(buyer.getId())) {
            throw new RuntimeException("Cannot purchase your own product");
        }
        
        // Update product state
        product.setSold(true);
        product.setBuyer(buyer);
        product.setSoldAt(LocalDateTime.now());
        
        // Save the updated product
        productRepository.save(product);
    }
} 