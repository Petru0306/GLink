package com.greenlink.greenlink.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/auth-test")
public class AuthTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthTestController.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @GetMapping("/admin")
    @ResponseBody
    public String adminTest() {
        return "Admin access granted!";
    }

    @GetMapping("/user")
    @ResponseBody
    public String userTest() {
        return "User access granted!";
    }
    
    @GetMapping("/generate-password")
    @ResponseBody
    public String generatePassword(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }
} 