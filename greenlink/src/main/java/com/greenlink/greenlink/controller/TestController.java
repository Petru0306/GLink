package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.service.ChallengeTrackingService;
import com.greenlink.greenlink.service.ReCaptchaService;
import com.greenlink.greenlink.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private ChallengeTrackingService challengeTrackingService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ReCaptchaService reCaptchaService;
    
    @Value("${recaptcha.enabled:false}")
    private boolean recaptchaEnabled;
    
    @Value("${recaptcha.site-key}")
    private String recaptchaSiteKey;
    
    @Value("${recaptcha.secret-key}")
    private String recaptchaSecretKey;

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

    @GetMapping("/recalculate-levels")
    @ResponseBody
    public String recalculateAllUserLevels() {
        try {
            userService.recalculateAllUserLevels(); 
            return "Successfully recalculated all user levels";
        } catch (Exception e) {
            return "Error recalculating levels: " + e.getMessage();
        }
    }

    @GetMapping("/recalculate-user-level")
    @ResponseBody
    public String recalculateUserLevel(@RequestParam Long userId) {
        try {
            userService.recalculateUserLevel(userId);
            return "Successfully recalculated level for user: " + userId;
        } catch (Exception e) {
            return "Error recalculating level for user " + userId + ": " + e.getMessage();
        }
    }

    @GetMapping("/test-recaptcha")
    @ResponseBody
    public Map<String, Object> testRecaptchaConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("recaptchaEnabled", recaptchaEnabled);
        result.put("recaptchaSiteKey", recaptchaSiteKey != null ? recaptchaSiteKey.substring(0, Math.min(10, recaptchaSiteKey.length())) + "..." : "null");
        result.put("recaptchaSecretKey", recaptchaSecretKey != null ? recaptchaSecretKey.substring(0, Math.min(10, recaptchaSecretKey.length())) + "..." : "null");
        result.put("timestamp", new java.util.Date());
        return result;
    }

    @GetMapping("/test-recaptcha-validation")
    @ResponseBody
    public Map<String, Object> testRecaptchaValidation(@RequestParam String token, @RequestParam(required = false) String remoteIp) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean isValid = reCaptchaService.validateCaptcha(token, remoteIp);
            
            result.put("success", true);
            result.put("token", token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "null");
            result.put("remoteIp", remoteIp);
            result.put("isValid", isValid);
            result.put("recaptchaEnabled", recaptchaEnabled);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            logger.error("Error testing reCAPTCHA validation", e);
        }
        
        return result;
    }
} 