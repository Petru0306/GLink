package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Conversation;
import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.ConversationRepository;
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
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    /**
     * Process a successful payment and update product state
     */
    @Transactional
    public void processSuccessfulPayment(String sessionId) throws StripeException {
        System.out.println("=== PROCESSING SUCCESSFUL PAYMENT ===");
        System.out.println("Session ID: " + sessionId);
        
        Session session = stripeService.retrieveSession(sessionId);
        System.out.println("Session retrieved: " + session.getId());
        System.out.println("Session metadata: " + session.getMetadata());
        
        // Extract metadata
        String productIdStr = session.getMetadata().get("product_id");
        String buyerIdStr = session.getMetadata().get("buyer_id");
        
        System.out.println("Product ID from metadata: " + productIdStr);
        System.out.println("Buyer ID from metadata: " + buyerIdStr);
        
        if (productIdStr == null || buyerIdStr == null) {
            throw new RuntimeException("Missing required metadata in session");
        }
        
        Long productId = Long.parseLong(productIdStr);
        Long buyerId = Long.parseLong(buyerIdStr);
        
        System.out.println("Parsed Product ID: " + productId);
        System.out.println("Parsed Buyer ID: " + buyerId);
        
        // Get product and buyer
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));
        
        System.out.println("Product found: " + product.getName() + " (ID: " + product.getId() + ")");
        System.out.println("Buyer found: " + buyer.getEmail() + " (ID: " + buyer.getId() + ")");
        System.out.println("Product currently sold: " + product.isSold());
        
        // Check if product is still available
        if (product.isSold()) {
            throw new RuntimeException("Product is already sold");
        }
        
        // Update product state
        product.setSold(true);
        product.setBuyer(buyer);
        product.setSoldAt(LocalDateTime.now());
        
        System.out.println("Updating product - sold: " + product.isSold() + ", buyer: " + product.getBuyer().getEmail());
        
        // Create delivery conversation
        Conversation deliveryConversation = Conversation.builder()
                .product(product)
                .seller(product.getSeller())
                .buyer(buyer)
                .status(Conversation.ConversationStatus.OPEN)
                .deliveryStatus(Conversation.DeliveryStatus.PENDING)
                .build();
        
        deliveryConversation = conversationRepository.save(deliveryConversation);
        
        // Link conversation to product
        product.setDeliveryConversation(deliveryConversation);
        
        productRepository.save(product);
        
        System.out.println("Product saved successfully!");
        System.out.println("Delivery conversation created with ID: " + deliveryConversation.getId());
        System.out.println("=== PAYMENT PROCESSING COMPLETE ===");
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
        
        // Extract domain from success URL for image URL conversion
        String domain = extractDomainFromUrl(successUrl);
        
        // Create checkout session
        Session session = stripeService.createCheckoutSession(product, buyer, successUrl, cancelUrl, domain);
        
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
        
        // Ensure seller has a Stripe account
        if (product.getSeller().getStripeAccountId() == null) {
            throw new RuntimeException("Seller must complete Stripe onboarding before accepting payments");
        }
        
        // Extract domain from success URL for image URL conversion
        String domain = extractDomainFromUrl(successUrl);
        
        // Create checkout session
        Session session = stripeService.createCheckoutSession(product, currentUser, successUrl, cancelUrl, domain);
        
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
    
    /**
     * Extract domain from URL
     */
    private String extractDomainFromUrl(String url) {
        try {
            if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                java.net.URL urlObj = new java.net.URL(url);
                return urlObj.getProtocol() + "://" + urlObj.getHost() + 
                       (urlObj.getPort() != -1 ? ":" + urlObj.getPort() : "");
            }
        } catch (Exception e) {
            // If URL parsing fails, return default domain
        }
        return "http://localhost:8080"; // Default fallback
    }
} 