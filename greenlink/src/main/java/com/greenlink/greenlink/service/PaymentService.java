package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Conversation;
import com.greenlink.greenlink.model.Message;
import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.ConversationRepository;
import com.greenlink.greenlink.repository.MessageRepository;
import com.greenlink.greenlink.repository.ProductRepository;
import com.greenlink.greenlink.repository.UserRepository;
import com.greenlink.greenlink.service.ChallengeTrackingService;
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
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ChallengeTrackingService challengeTrackingService;
    
    
    @Transactional
    public void processSuccessfulPayment(String sessionId) throws StripeException {
        System.out.println("=== PROCESSING SUCCESSFUL PAYMENT ===");
        System.out.println("Session ID: " + sessionId);
        
        Session session = stripeService.retrieveSession(sessionId);
        System.out.println("Session retrieved: " + session.getId());
        System.out.println("Session metadata: " + session.getMetadata());
        
        
        String productIdStr = session.getMetadata().get("product_id");
        String buyerIdStr = session.getMetadata().get("buyer_id");
        String priceUsedStr = session.getMetadata().get("price_used");
        String isNegotiatedStr = session.getMetadata().get("is_negotiated_price");
        
        System.out.println("Product ID from metadata: " + productIdStr);
        System.out.println("Buyer ID from metadata: " + buyerIdStr);
        System.out.println("Price used: " + priceUsedStr + " RON");
        System.out.println("Was negotiated price: " + isNegotiatedStr);
        
        if (productIdStr == null || buyerIdStr == null) {
            throw new RuntimeException("Missing required metadata in session");
        }
        
        Long productId = Long.parseLong(productIdStr);
        Long buyerId = Long.parseLong(buyerIdStr);
        
        System.out.println("Parsed Product ID: " + productId);
        System.out.println("Parsed Buyer ID: " + buyerId);
        
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));
        
        System.out.println("Product found: " + product.getName() + " (ID: " + product.getId() + ")");
        System.out.println("Buyer found: " + buyer.getEmail() + " (ID: " + buyer.getId() + ")");
        System.out.println("Product currently sold: " + product.isSold());
        System.out.println("Product current buyer: " + (product.getBuyer() != null ? product.getBuyer().getEmail() : "null"));
        
        
        if (product.isSold()) {
            throw new RuntimeException("Product is already sold");
        }
        
        
        product.setSold(true);
        product.setBuyer(buyer);
        product.setSoldAt(LocalDateTime.now());
        
        System.out.println("Updating product - sold: " + product.isSold() + ", buyer: " + product.getBuyer().getEmail());
        System.out.println("Sold at: " + product.getSoldAt());
        
        
        Conversation deliveryConversation = Conversation.builder()
                .product(product)
                .seller(product.getSeller())
                .buyer(buyer)
                .build();
        
        deliveryConversation = conversationRepository.save(deliveryConversation);
        System.out.println("=== DELIVERY CONVERSATION CREATED ===");
        System.out.println("Conversation ID: " + deliveryConversation.getId());
        System.out.println("Product: " + deliveryConversation.getProduct().getName());
        System.out.println("Seller: " + deliveryConversation.getSeller().getEmail());
        System.out.println("Buyer: " + deliveryConversation.getBuyer().getEmail());
        System.out.println("Created at: " + deliveryConversation.getCreatedAt());
        
        
        String welcomeMessageContent;
        if (priceUsedStr != null && isNegotiatedStr != null && "true".equals(isNegotiatedStr)) {
            welcomeMessageContent = String.format("Hi! Your order for '%s' has been confirmed at the negotiated price of %s RON. Let's coordinate the delivery details.", 
                product.getName(), priceUsedStr);
        } else {
            welcomeMessageContent = String.format("Hi! Your order for '%s' has been confirmed at %s RON. Let's coordinate the delivery details.", 
                product.getName(), priceUsedStr != null ? priceUsedStr : String.valueOf(product.getPrice()));
        }
        
        Message welcomeMessage = Message.builder()
                .conversation(deliveryConversation)
                .sender(product.getSeller())
                .content(welcomeMessageContent)
                .build();
        
        messageRepository.save(welcomeMessage);
        System.out.println("Welcome message added to delivery conversation");
        System.out.println("Message ID: " + welcomeMessage.getId());
        System.out.println("=== DELIVERY CONVERSATION SETUP COMPLETE ===");
        
        
        Product savedProduct = productRepository.save(product);
        System.out.println("Product saved successfully! ID: " + savedProduct.getId());
        System.out.println("Saved product sold status: " + savedProduct.isSold());
        System.out.println("Saved product buyer: " + (savedProduct.getBuyer() != null ? savedProduct.getBuyer().getEmail() : "null"));
        
        
        Product verifyProduct = productRepository.findById(productId).orElse(null);
        if (verifyProduct != null) {
            System.out.println("VERIFICATION - Product from DB - sold: " + verifyProduct.isSold() + ", buyer: " + (verifyProduct.getBuyer() != null ? verifyProduct.getBuyer().getEmail() : "null"));
        } else {
            System.out.println("VERIFICATION - Product not found in DB after save!");
        }
        
        System.out.println("=== PAYMENT PROCESSING COMPLETE ===");
    }
    
    
    public String createCheckoutSession(Long productId, String successUrl, String cancelUrl) throws StripeException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.isSold()) {
            throw new RuntimeException("Product is already sold");
        }
        

        User buyer = getCurrentUser();
        
        
        if (buyer.getStripeCustomerId() == null) {
            stripeService.createCustomer(buyer);
            userRepository.save(buyer);
        }
        
        String domain = extractDomainFromUrl(successUrl);
        
        Session session = stripeService.createCheckoutSession(product, buyer, successUrl, cancelUrl, domain);
        
        return session.getUrl();
    }
    
    private User getCurrentUser() {
        throw new RuntimeException("Current user should be passed from controller");
    }
    
    public String createCheckoutSession(Long productId, User currentUser, String successUrl, String cancelUrl) throws StripeException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.isSold()) {
            throw new RuntimeException("Product is already sold");
        }
        
        if (currentUser.getStripeCustomerId() == null) {
            stripeService.createCustomer(currentUser);
            userRepository.save(currentUser);
        }
        
        if (product.getSeller().getStripeAccountId() == null) {
            throw new RuntimeException("Seller must complete Stripe onboarding before accepting payments");
        }
        
        String domain = extractDomainFromUrl(successUrl);
        
        Session session = stripeService.createCheckoutSession(product, currentUser, successUrl, cancelUrl, domain);
        
        return session.getUrl();
    }
    
    @Transactional
    public void processAdminPurchase(Product product, User buyer) {
        System.out.println("=== PROCESSING ADMIN PURCHASE ===");
        System.out.println("Product: " + product.getName() + " (ID: " + product.getId() + ")");
        System.out.println("Buyer: " + buyer.getEmail() + " (ID: " + buyer.getId() + ")");
        System.out.println("Product currently sold: " + product.isSold());
        System.out.println("Product current buyer: " + (product.getBuyer() != null ? product.getBuyer().getEmail() : "null"));
        
        if (product.isSold()) {
            throw new RuntimeException("Product is already sold");
        }
        
        if (product.getSeller().getId().equals(buyer.getId())) {
            throw new RuntimeException("Cannot purchase your own product");
        }
        
        product.setSold(true);
        product.setBuyer(buyer);
        product.setSoldAt(LocalDateTime.now());
        
        System.out.println("Updating product - sold: " + product.isSold() + ", buyer: " + product.getBuyer().getEmail());
        System.out.println("Sold at: " + product.getSoldAt());

        Conversation deliveryConversation = Conversation.builder()
                .product(product)
                .seller(product.getSeller())
                .buyer(buyer)
                .build();
        
        deliveryConversation = conversationRepository.save(deliveryConversation);
        System.out.println("Delivery conversation created with ID: " + deliveryConversation.getId());
        
        
        Double negotiatedPrice = product.getNegotiatedPriceForUser(buyer.getId());
        String welcomeMessageContent;
        if (negotiatedPrice != null) {
            welcomeMessageContent = String.format("Hi! Your order for '%s' has been confirmed at the negotiated price of %.2f RON. Let's coordinate the delivery details.", 
                product.getName(), negotiatedPrice);
        } else {
            welcomeMessageContent = String.format("Hi! Your order for '%s' has been confirmed at %.2f RON. Let's coordinate the delivery details.", 
                product.getName(), product.getPrice());
        }
        
        Message welcomeMessage = Message.builder()
                .conversation(deliveryConversation)
                .sender(product.getSeller())
                .content(welcomeMessageContent)
                .build();
        
        messageRepository.save(welcomeMessage);
        System.out.println("Welcome message added to delivery conversation");
        
        
        Product savedProduct = productRepository.save(product);
        System.out.println("Product saved successfully! ID: " + savedProduct.getId());
        System.out.println("Saved product sold status: " + savedProduct.isSold());
        System.out.println("Saved product buyer: " + (savedProduct.getBuyer() != null ? savedProduct.getBuyer().getEmail() : "null"));
        
        // Track challenge progress for buying items
        challengeTrackingService.trackUserAction(buyer.getId(), "MARKETPLACE_ITEM_PURCHASED", product.getId());
        
        
        Product verifyProduct = productRepository.findById(product.getId()).orElse(null);
        if (verifyProduct != null) {
            System.out.println("VERIFICATION - Product from DB - sold: " + verifyProduct.isSold() + ", buyer: " + (verifyProduct.getBuyer() != null ? verifyProduct.getBuyer().getEmail() : "null"));
        } else {
            System.out.println("VERIFICATION - Product not found in DB after save!");
        }
        
        System.out.println("=== ADMIN PURCHASE COMPLETE ===");
    }
    
    
    private String extractDomainFromUrl(String url) {
        try {
            if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                java.net.URL urlObj = new java.net.URL(url);
                return urlObj.getProtocol() + "://" + urlObj.getHost() + 
                       (urlObj.getPort() != -1 ? ":" + urlObj.getPort() : "");
            }
        } catch (Exception e) {
            
        }
        return "http://localhost:8080"; 
    }
} 