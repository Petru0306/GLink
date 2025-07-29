package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.ReCaptchaService;
import com.greenlink.greenlink.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ReCaptchaService reCaptchaService;
    
    @Value("${recaptcha.site-key}")
    private String recaptchaSiteKey;
    
    @Value("${recaptcha.enabled:false}")
    private boolean recaptchaEnabled;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, ReCaptchaService reCaptchaService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.reCaptchaService = reCaptchaService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", new User());
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        model.addAttribute("recaptchaEnabled", recaptchaEnabled);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                              BindingResult result, 
                              Model model,
                              @RequestParam("confirmPassword") String confirmPassword,
                              @RequestParam(value = "g-recaptcha-response", required = false) String recaptchaResponse,
                              HttpServletRequest request) {
        logger.debug("Registration attempt for email: {}", user.getEmail());
        logger.debug("reCAPTCHA enabled: {}, response present: {}", recaptchaEnabled, recaptchaResponse != null && !recaptchaResponse.isEmpty());
        
        if (result.hasErrors()) {
            logger.debug("Validation errors found: {}", result.getAllErrors());
            model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
            model.addAttribute("recaptchaEnabled", recaptchaEnabled);
            return "register";
        }

        // Check if passwords match
        if (!user.getPassword().equals(confirmPassword)) {
            logger.debug("Password mismatch detected");
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
            model.addAttribute("recaptchaEnabled", recaptchaEnabled);
            return "register";
        }
        
        // Validate reCAPTCHA if enabled
        if (recaptchaEnabled) {
            String clientIp = request.getRemoteAddr();
            logger.debug("Validating reCAPTCHA for IP: {}", clientIp);
            boolean isValidCaptcha = reCaptchaService.validateCaptcha(recaptchaResponse, clientIp);
            logger.debug("reCAPTCHA validation result: {}", isValidCaptcha);
            
            if (!isValidCaptcha) {
                logger.warn("reCAPTCHA validation failed for user: {}", user.getEmail());
                model.addAttribute("error", "Please validate the reCAPTCHA");
                model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
                model.addAttribute("recaptchaEnabled", recaptchaEnabled);
                return "register";
            }
        }

        try {
            logger.debug("Attempting to register user: {}", user.getEmail());
            userService.registerUser(user);
            logger.info("User registered successfully: {}", user.getEmail());
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            logger.error("Registration failed for user: {}", user.getEmail(), e);
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

    @GetMapping("/auth-test/generate-password")
    @ResponseBody
    public String generatePasswordHash(@RequestParam String password) {
        String encodedPassword = passwordEncoder.encode(password);
        return "Password hash for '" + password + "': " + encodedPassword;
    }
}