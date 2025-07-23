package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.service.ChallengeTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private ChallengeTrackingService challengeTrackingService;

    @GetMapping("/test-offer-challenge")
    public String testOfferChallenge(@RequestParam Long userId) {
        try {
            challengeTrackingService.trackUserAction(userId, "MARKETPLACE_OFFER_MADE", null);
            return "Offer challenge tracking triggered for user: " + userId;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @GetMapping("/sell")
    public String testSellPage(Model model) {
        logger.info("Test sell page accessed");
        model.addAttribute("message", "This is a test sell page");
        model.addAttribute("product", new com.greenlink.greenlink.dto.ProductDto());
        model.addAttribute("isSelling", true);
        return "marketplace/product-form";
    }
    
    @GetMapping("/debug/product-form")
    public String debugProductForm(Model model) {
        logger.info("Debug product form accessed");
        
        // Create a ProductDto with all fields set
        com.greenlink.greenlink.dto.ProductDto product = new com.greenlink.greenlink.dto.ProductDto();
        product.setName("Test Product");
        product.setPrice(10.0);
        product.setStock(5);
        product.setDescription("Test description");
        product.setCategory(com.greenlink.greenlink.model.Product.Category.BIO);
        product.setBranch(com.greenlink.greenlink.model.Product.Branch.VERDE);
        product.setEcoFriendly(true);
        
        model.addAttribute("product", product);
        model.addAttribute("isSelling", true);
        model.addAttribute("debug", true);
        
        logger.info("Product branch: {}", product.getBranch());
        logger.info("Product category: {}", product.getCategory());
        
        return "marketplace/product-form";
    }
} 