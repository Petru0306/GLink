package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(user);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Authentication authentication) {
        logger.debug("Showing login form. Authentication: {}", authentication);
        if (authentication != null && authentication.isAuthenticated()) {
            logger.debug("User is already authenticated, redirecting to dashboard");
            return "redirect:/dashboard";
        }
        logger.debug("User is not authenticated, showing login form");
        return "login";
    }

    @GetMapping("/login-error")
    public String loginError(Model model) {
        logger.error("Login attempt failed");
        model.addAttribute("loginError", true);
        return "login";
    }
}