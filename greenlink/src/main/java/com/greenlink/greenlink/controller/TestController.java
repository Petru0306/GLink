package com.greenlink.greenlink.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class TestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    @GetMapping("/simple")
    public String testSimplePage(Model model) {
        logger.info("Test simple page accessed");
        model.addAttribute("message", "This is a test page");
        return "test-simple";
    }
    
    @GetMapping("/marketplace")
    public String testMarketplacePage(Model model) {
        logger.info("Test marketplace page accessed");
        try {
            model.addAttribute("message", "This is a test marketplace page");
            model.addAttribute("products", java.util.List.of());
            model.addAttribute("isMyProducts", true);
            model.addAttribute("currentUser", null); // Add this to prevent template errors
            logger.info("Test marketplace page model attributes set successfully");
            return "test-my-products"; // Use simplified template
        } catch (Exception e) {
            logger.error("Error in test marketplace page", e);
            model.addAttribute("error", "Test page error: " + e.getMessage());
            return "error";
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
} 