package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.service.PaymentService;
import com.greenlink.greenlink.service.StripeService;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ProductService;
import com.greenlink.greenlink.repository.ProductRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private StripeService stripeService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Value("${stripe.publishable-key}")
    private String publishableKey;
    
    /**
     * Create checkout session for a product
     */
    @PostMapping("/create-checkout/{productId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @PathVariable Long productId,
            @RequestParam String successUrl,
            @RequestParam String cancelUrl) {
        
        try {
            // Validate URLs
            if (successUrl == null || successUrl.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Success URL is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (cancelUrl == null || cancelUrl.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cancel URL is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Clean and validate URLs
            String cleanSuccessUrl = successUrl.trim();
            String cleanCancelUrl = cancelUrl.trim();
            
            // Ensure URLs are absolute
            if (!cleanSuccessUrl.startsWith("http://") && !cleanSuccessUrl.startsWith("https://")) {
                cleanSuccessUrl = "https://" + cleanSuccessUrl.replaceFirst("^/+", "");
            }
            
            if (!cleanCancelUrl.startsWith("http://") && !cleanCancelUrl.startsWith("https://")) {
                cleanCancelUrl = "https://" + cleanCancelUrl.replaceFirst("^/+", "");
            }
            
            logger.info("Creating checkout session for product {} with successUrl: {} and cancelUrl: {}", 
                       productId, cleanSuccessUrl, cleanCancelUrl);
            
            User currentUser = userService.getCurrentUser();
            String checkoutUrl = paymentService.createCheckoutSession(productId, currentUser, cleanSuccessUrl, cleanCancelUrl);
            
            Map<String, String> response = new HashMap<>();
            response.put("checkoutUrl", checkoutUrl);
            
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            logger.error("Error creating checkout session", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create checkout session: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error creating checkout session", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create checkout session: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Handle Stripe webhooks
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request) {
        String payload = "";
        String signature = request.getHeader("Stripe-Signature");
        
        logger.info("Webhook received - Signature: {}", signature != null ? "Present" : "Missing");
        
        try {
            // Read the request body
            java.io.BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            payload = sb.toString();
            
            logger.info("Webhook payload received, length: {}", payload.length());
            
            // Verify webhook signature
            if (!stripeService.verifyWebhookSignature(payload, signature)) {
                logger.error("Invalid webhook signature");
                return ResponseEntity.badRequest().body("Invalid signature");
            }
            
            logger.info("Webhook signature verified successfully");
            
            // Parse the event
            Event event = com.stripe.net.Webhook.constructEvent(payload, signature, stripeService.getWebhookSecret());
            
            logger.info("Webhook event parsed: {}", event.getType());
            
            // Handle the event
            switch (event.getType()) {
                case "checkout.session.completed":
                    logger.info("Processing checkout.session.completed event");
                    handleCheckoutSessionCompleted(event);
                    break;
                case "account.updated":
                    logger.info("Processing account.updated event");
                    handleAccountUpdated(event);
                    break;
                default:
                    logger.info("Unhandled event type: {}", event.getType());
            }
            
            logger.info("Webhook processed successfully");
            return ResponseEntity.ok("Webhook processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.badRequest().body("Webhook processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Handle successful checkout completion
     */
    private void handleCheckoutSessionCompleted(Event event) {
        try {
            com.stripe.model.checkout.Session session = (com.stripe.model.checkout.Session) event.getData().getObject();
            logger.info("Processing successful payment for session: {}", session.getId());
            logger.info("Session metadata: {}", session.getMetadata());
            
            paymentService.processSuccessfulPayment(session.getId());
            logger.info("Payment processed successfully for session: {}", session.getId());
        } catch (Exception e) {
            logger.error("Error processing successful payment", e);
            throw e; // Re-throw to ensure webhook fails
        }
    }
    
    /**
     * Handle account updates
     */
    private void handleAccountUpdated(Event event) {
        try {
            com.stripe.model.Account account = (com.stripe.model.Account) event.getData().getObject();
            logger.info("Account updated: {}", account.getId());
        } catch (Exception e) {
            logger.error("Error handling account update", e);
        }
    }
    
    /**
     * Onboard seller to Stripe Connect
     */
    @PostMapping("/onboard-seller")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, String>> onboardSeller(@RequestParam String returnUrl) {
        logger.info("Starting seller onboarding process for returnUrl: {}", returnUrl);
        
        try {
            User currentUser = userService.getCurrentUser();
            logger.info("Current user: {} (ID: {})", currentUser.getEmail(), currentUser.getId());
            
            // Create Stripe Connect account if not exists
            if (currentUser.getStripeAccountId() == null) {
                logger.info("Creating new Stripe Connect account for user: {}", currentUser.getEmail());
                stripeService.createConnectAccount(currentUser);
                userService.updateUser(currentUser);
                logger.info("Stripe Connect account created: {}", currentUser.getStripeAccountId());
            } else {
                logger.info("User already has Stripe account: {}", currentUser.getStripeAccountId());
            }
            
            // Create account link for onboarding
            logger.info("Creating account link for onboarding");
            String accountLinkUrl = stripeService.createAccountLink(currentUser.getStripeAccountId(), returnUrl);
            logger.info("Account link created: {}", accountLinkUrl);
            
            Map<String, String> response = new HashMap<>();
            response.put("accountLinkUrl", accountLinkUrl);
            
            logger.info("Onboarding process completed successfully");
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            logger.error("Stripe error during seller onboarding", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to onboard seller: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Unexpected error during seller onboarding", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to onboard seller: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get seller account status
     */
    @GetMapping("/account-status")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAccountStatus() {
        try {
            User currentUser = userService.getCurrentUser();
            
            if (currentUser.getStripeAccountId() == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("onboarded", false);
                response.put("message", "Seller not onboarded");
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> status = stripeService.getAccountStatus(currentUser.getStripeAccountId());
            status.put("onboarded", true);
            
            return ResponseEntity.ok(status);
        } catch (StripeException e) {
            logger.error("Error getting account status", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get account status: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error getting account status", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get account status: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Show payment success page
     */
    @GetMapping("/success")
    public String showSuccessPage(Model model) {
        return "payment/success";
    }
    
    /**
     * Show payment cancel page
     */
    @GetMapping("/cancel")
    public String showCancelPage(Model model) {
        return "payment/cancel";
    }
    
    /**
     * Test endpoint to verify payment controller is working
     */
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<Map<String, String>> testEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Payment controller is working");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Handle return from Stripe onboarding
     */
    @GetMapping("/onboarding-return")
    public String handleOnboardingReturn(@RequestParam(required = false) String refresh, Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            
            if (refresh != null) {
                // User needs to refresh their onboarding
                model.addAttribute("message", "Please complete your onboarding to continue selling.");
                return "marketplace/seller-onboarding";
            }
            
            // Check if user has completed onboarding
            if (currentUser.getStripeAccountId() != null) {
                Map<String, Object> status = stripeService.getAccountStatus(currentUser.getStripeAccountId());
                boolean isComplete = (Boolean) status.get("chargesEnabled") && (Boolean) status.get("detailsSubmitted");
                
                if (isComplete) {
                    model.addAttribute("success", true);
                    model.addAttribute("message", "Your seller account has been successfully set up!");
                    return "marketplace/onboarding-success";
                } else {
                    model.addAttribute("message", "Please complete your onboarding to continue selling.");
                    return "marketplace/seller-onboarding";
                }
            } else {
                model.addAttribute("message", "Please complete your onboarding to continue selling.");
                return "marketplace/seller-onboarding";
            }
        } catch (Exception e) {
            logger.error("Error handling onboarding return", e);
            model.addAttribute("error", "An error occurred while processing your onboarding return.");
            return "marketplace/seller-onboarding";
        }
    }
    
    /**
     * Test endpoint to manually process a payment (for debugging)
     */
    @PostMapping("/test-process-payment")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testProcessPayment(@RequestParam Long productId, @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check admin password
            String adminPassword = "admin123";
            
            if (!adminPassword.equals(password)) {
                response.put("success", false);
                response.put("message", "Incorrect password");
                return ResponseEntity.ok(response);
            }
            
            // Get current user and product
            User buyer = userService.getCurrentUser();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            if (product.getSeller().getId().equals(buyer.getId())) {
                response.put("success", false);
                response.put("message", "Cannot purchase your own product");
                return ResponseEntity.ok(response);
            }
            
            // Process the purchase manually
            paymentService.processAdminPurchase(product, buyer);
            
            response.put("success", true);
            response.put("message", "Product purchased successfully!");
            response.put("productId", productId);
            response.put("buyerId", buyer.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing test purchase", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Admin purchase endpoint - allows purchase with admin password
     */
    @PostMapping("/admin-purchase")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adminPurchase(@RequestParam Long productId, @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check admin password (you can change this to any password you want)
            String adminPassword = "admin123"; // TODO: Move to application.properties
            
            if (!adminPassword.equals(password)) {
                response.put("success", false);
                response.put("message", "Parolă incorectă");
                return ResponseEntity.ok(response);
            }
            
            // Get current user and product
            User buyer = userService.getCurrentUser();
            ProductDto productDto = productService.getProductById(productId);
            
            // Get the actual Product entity from repository
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));
            
            if (product == null) {
                response.put("success", false);
                response.put("message", "Produsul nu a fost găsit");
                return ResponseEntity.ok(response);
            }
            
            if (product.getSeller().getId().equals(buyer.getId())) {
                response.put("success", false);
                response.put("message", "Nu puteți achiziționa propriul produs");
                return ResponseEntity.ok(response);
            }
            
            // Process the purchase
            paymentService.processAdminPurchase(product, buyer);
            
            response.put("success", true);
            response.put("message", "Produsul a fost achiziționat cu succes!");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing admin purchase", e);
            response.put("success", false);
            response.put("message", "Eroare la procesarea achiziției: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
} 