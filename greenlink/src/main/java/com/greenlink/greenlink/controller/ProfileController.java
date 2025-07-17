package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ChallengeTrackingService;
import com.greenlink.greenlink.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Controller for handling user profile-related operations.
 * Manages both personal profiles and public profile views.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final UserService userService;
    private final ChallengeTrackingService challengeTrackingService;
    private final ProductService productService;

    @Autowired
    public ProfileController(UserService userService,
                           ChallengeTrackingService challengeTrackingService,
                           ProductService productService) {
        this.userService = userService;
        this.challengeTrackingService = challengeTrackingService;
        this.productService = productService;
    }

    /**
     * Display the current user's personal profile page.
     * Shows user's own data and quick actions.
     */
    @GetMapping
    public String showPersonalProfile(Model model, Principal principal) {
        try {
            if (principal == null) {
                logger.warn("Unauthenticated user attempted to access personal profile");
                return "redirect:/login";
            }

            User currentUser = userService.getCurrentUser(principal.getName());
            if (currentUser == null) {
                logger.error("Current user not found for principal: {}", principal.getName());
                model.addAttribute("error", "User session expired. Please login again.");
                return "error";
            }

            // Load user's basic data
            model.addAttribute("user", currentUser);
            model.addAttribute("totalPoints", currentUser.getPoints());
            
            logger.info("Personal profile loaded successfully for user: {}", currentUser.getUsername());
            return "profile/personal";

        } catch (Exception e) {
            logger.error("Error loading personal profile: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load profile. Please try again.");
            return "error";
        }
    }

    /**
     * Display the edit profile page for the current user.
     */
    @GetMapping("/edit")
    public String showEditProfile(Model model, Principal principal) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            User currentUser = userService.getCurrentUser(principal.getName());
            if (currentUser == null) {
                model.addAttribute("error", "User session expired. Please login again.");
                return "error";
            }

            model.addAttribute("user", currentUser);
            return "profile/edit";

        } catch (Exception e) {
            logger.error("Error loading edit profile page: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load edit profile page.");
            return "error";
        }
    }

    /**
     * Handle profile update form submission.
     */
    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute User userUpdate,
                              @RequestParam(required = false) MultipartFile profilePicture,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            User updatedUser = userService.updateProfile(principal.getName(), userUpdate, profilePicture);
            
            // Track profile photo upload for challenges if applicable
            if (profilePicture != null && !profilePicture.isEmpty()) {
                challengeTrackingService.trackUserAction(updatedUser.getId(), "PROFILE_PHOTO_UPLOADED", null);
                logger.info("Profile photo uploaded for user: {}", updatedUser.getUsername());
            }
            
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            logger.info("Profile updated successfully for user: {}", updatedUser.getUsername());

        } catch (Exception e) {
            logger.error("Error updating profile: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    /**
     * Display a public profile page for any user by ID.
     * Shows user's public information and products.
     */
    @GetMapping("/user/{userId}")
    public String viewPublicProfile(@PathVariable Long userId, Model model) {
        try {
            // Validate input
            if (userId == null || userId <= 0) {
                logger.warn("Invalid user ID provided: {}", userId);
                model.addAttribute("error", "Invalid user ID provided.");
                return "error";
            }

            // Get the target user
            User targetUser = userService.getUserById(userId);
            if (targetUser == null) {
                logger.warn("User with ID {} not found", userId);
                model.addAttribute("error", "User not found.");
                return "error";
            }

            // Load public profile data
            loadPublicProfileData(model, targetUser);
            
            logger.info("Public profile loaded successfully for user ID: {} ({})", userId, targetUser.getUsername());
            return "profile/public";

        } catch (Exception e) {
            logger.error("Error loading public profile for user ID {}: {}", userId, e.getMessage(), e);
            model.addAttribute("error", "Failed to load profile. Please try again later.");
            return "error";
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Load data for public profile page.
     */
    private void loadPublicProfileData(Model model, User targetUser) {
        // Basic user data
        model.addAttribute("profileUser", targetUser);

        // User's products
        try {
            List<ProductDto> userProducts = productService.getProductsBySeller(targetUser.getId());
            model.addAttribute("products", userProducts != null ? userProducts : List.of());
        } catch (Exception e) {
            logger.warn("Failed to load products for user {}: {}", targetUser.getId(), e.getMessage());
            model.addAttribute("products", List.of());
        }
    }
}