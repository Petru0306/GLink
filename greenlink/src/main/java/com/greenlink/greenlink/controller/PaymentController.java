package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.PaymentService;
import com.greenlink.greenlink.service.StripeService;
import com.greenlink.greenlink.service.UserService;
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
            User currentUser = userService.getCurrentUser();
            String checkoutUrl = paymentService.createCheckoutSession(productId, currentUser, successUrl, cancelUrl);
            
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
        
        try {
            // Read the request body
            java.io.BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            payload = sb.toString();
            
            // Verify webhook signature
            if (!stripeService.verifyWebhookSignature(payload, signature)) {
                logger.error("Invalid webhook signature");
                return ResponseEntity.badRequest().body("Invalid signature");
            }
            
            // Parse the event
            Event event = com.stripe.net.Webhook.constructEvent(payload, signature, stripeService.getWebhookSecret());
            
            // Handle the event
            switch (event.getType()) {
                case "checkout.session.completed":
                    handleCheckoutSessionCompleted(event);
                    break;
                case "account.updated":
                    handleAccountUpdated(event);
                    break;
                default:
                    logger.info("Unhandled event type: {}", event.getType());
            }
            
            return ResponseEntity.ok("Webhook processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.badRequest().body("Webhook processing failed");
        }
    }
    
    /**
     * Handle successful checkout completion
     */
    private void handleCheckoutSessionCompleted(Event event) {
        try {
            com.stripe.model.checkout.Session session = (com.stripe.model.checkout.Session) event.getData().getObject();
            paymentService.processSuccessfulPayment(session.getId());
            logger.info("Payment processed successfully for session: {}", session.getId());
        } catch (Exception e) {
            logger.error("Error processing successful payment", e);
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
        try {
            User currentUser = userService.getCurrentUser();
            
            // Create Stripe Connect account if not exists
            if (currentUser.getStripeAccountId() == null) {
                stripeService.createConnectAccount(currentUser);
                userService.updateUser(currentUser);
            }
            
            // Create account link for onboarding
            String accountLinkUrl = stripeService.createAccountLink(currentUser.getStripeAccountId(), returnUrl);
            
            Map<String, String> response = new HashMap<>();
            response.put("accountLinkUrl", accountLinkUrl);
            
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            logger.error("Error onboarding seller", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to onboard seller: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error onboarding seller", e);
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
} 