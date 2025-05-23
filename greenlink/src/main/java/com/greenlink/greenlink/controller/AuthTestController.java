package com.greenlink.greenlink.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth-test")
public class AuthTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthTestController.class);

    @GetMapping("/status")
    public String getAuthStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Current authentication: {}", auth);
        if (auth != null && auth.isAuthenticated()) {
            return "Authenticated as: " + auth.getName() + 
                   ", Authorities: " + auth.getAuthorities();
        }
        return "Not authenticated";
    }
} 