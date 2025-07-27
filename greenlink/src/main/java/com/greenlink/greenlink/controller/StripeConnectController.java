package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.config.StripeConfig;
import com.greenlink.greenlink.dto.StripeConnectAccountDto;
import com.greenlink.greenlink.dto.StripeProductDto;
import com.greenlink.greenlink.dto.StripeCheckoutDto;
import com.greenlink.greenlink.service.StripeConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller for Stripe Connect operations
 * Handles account creation, onboarding, product management, and checkout
 */
@Controller
@RequestMapping("/stripe-connect")
public class StripeConnectController {
    
    @Autowired
    private StripeConnectService stripeConnectService;
    
    @Autowired
    private StripeConfig stripeConfig;
    
    /**
     * Display the Stripe Connect dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("publishableKey", stripeConfig.getPublishableKey());
        return "stripe-connect/dashboard";
    }
    
    /**
     * Display the account creation form
     */
    @GetMapping("/create-account")
    public String createAccountForm(Model model) {
        model.addAttribute("publishableKey", stripeConfig.getPublishableKey());
        return "stripe-connect/create-account";
    }
    
    /**
     * Create a new connected account
     */
    @PostMapping("/create-account")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody StripeConnectAccountDto accountDto) {
        try {
            StripeConnectAccountDto createdAccount = stripeConnectService.createConnectedAccount(accountDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("account", createdAccount);
            response.put("message", "Connected account created successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Display account status and onboarding
     */
    @GetMapping("/account/{accountId}")
    public String accountStatus(@PathVariable String accountId, Model model) {
        try {
            StripeConnectAccountDto account = stripeConnectService.getAccountStatus(accountId);
            model.addAttribute("account", account);
            model.addAttribute("publishableKey", stripeConfig.getPublishableKey());
            return "stripe-connect/account-status";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "stripe-connect/error";
        }
    }
    
    /**
     * Create an account link for onboarding
     */
    @PostMapping("/account/{accountId}/onboard")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOnboardingLink(
            @PathVariable String accountId,
            @RequestParam String refreshUrl,
            @RequestParam String returnUrl) {
        
        try {
            String accountLinkUrl = stripeConnectService.createAccountLink(accountId, refreshUrl, returnUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accountLinkUrl", accountLinkUrl);
            response.put("message", "Onboarding link created successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Display product creation form
     */
    @GetMapping("/account/{accountId}/products/create")
    public String createProductForm(@PathVariable String accountId, Model model) {
        model.addAttribute("accountId", accountId);
        model.addAttribute("publishableKey", stripeConfig.getPublishableKey());
        return "stripe-connect/create-product";
    }
    
    /**
     * Create a new product
     */
    @PostMapping("/account/{accountId}/products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createProduct(
            @PathVariable String accountId,
            @RequestBody StripeProductDto productDto) {
        
        try {
            productDto.setConnectedAccountId(accountId);
            StripeProductDto createdProduct = stripeConnectService.createProduct(productDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("product", createdProduct);
            response.put("message", "Product created successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * List all products for a connected account
     */
    @GetMapping("/account/{accountId}/products")
    public String listProducts(@PathVariable String accountId, Model model) {
        try {
            List<StripeProductDto> products = stripeConnectService.getProducts(accountId);
            model.addAttribute("products", products);
            model.addAttribute("accountId", accountId);
            model.addAttribute("publishableKey", stripeConfig.getPublishableKey());
            return "stripe-connect/products";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "stripe-connect/error";
        }
    }
    
    /**
     * Display storefront for a connected account
     * Note: In production, you should use a different identifier instead of account ID
     */
    @GetMapping("/store/{accountId}")
    public String storefront(@PathVariable String accountId, Model model) {
        try {
            List<StripeProductDto> products = stripeConnectService.getProducts(accountId);
            model.addAttribute("products", products);
            model.addAttribute("accountId", accountId);
            model.addAttribute("publishableKey", stripeConfig.getPublishableKey());
            return "stripe-connect/storefront";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "stripe-connect/error";
        }
    }
    
    /**
     * Create a checkout session for purchasing products
     */
    @PostMapping("/checkout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCheckout(@RequestBody StripeCheckoutDto checkoutDto) {
        try {
            // Calculate application fee (platform commission)
            Long totalAmount = checkoutDto.getLineItems().stream()
                .mapToLong(item -> {
                    // In a real application, you would fetch the product price from the database
                    // For this demo, we'll use a placeholder
                    return 1000L * item.getQuantity(); // $10.00 per item
                })
                .sum();
            
            Long applicationFee = (long) (totalAmount * (stripeConfig.getPlatformCommissionPercentage() / 100.0));
            checkoutDto.setApplicationFeeAmount(applicationFee);
            
            StripeCheckoutDto checkoutSession = stripeConnectService.createCheckoutSession(checkoutDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("checkoutUrl", checkoutSession.getCheckoutUrl());
            response.put("sessionId", checkoutSession.getSessionId());
            response.put("message", "Checkout session created successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Handle successful payment redirect
     */
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam String session_id, Model model) {
        try {
            // In a real application, you would verify the session and update your database
            model.addAttribute("sessionId", session_id);
            model.addAttribute("message", "Payment completed successfully!");
            return "stripe-connect/payment-success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "stripe-connect/error";
        }
    }
    
    /**
     * Handle cancelled payment redirect
     */
    @GetMapping("/cancel")
    public String paymentCancel(Model model) {
        model.addAttribute("message", "Payment was cancelled.");
        return "stripe-connect/payment-cancel";
    }
} 