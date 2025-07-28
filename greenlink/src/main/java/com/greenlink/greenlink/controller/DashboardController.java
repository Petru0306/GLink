package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.model.PointEvent;
import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.dto.PurchaseDto;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ProductService;
import com.greenlink.greenlink.service.ChallengeService;
import com.greenlink.greenlink.service.PointsService;
import com.greenlink.greenlink.service.ProfilePictureStorageService;
import com.greenlink.greenlink.service.ChallengeTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ChallengeService challengeService;
    
    @Autowired
    private PointsService pointsService;
    
    @Autowired
    private ProfilePictureStorageService profilePictureStorageService;
    
    @Autowired
    private ChallengeTrackingService challengeTrackingService;

    public DashboardController(UserService userService) { 
        this.userService = userService;
    }

    @GetMapping
    public String showDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        
        User user = userService.getCurrentUser(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        
        
        try {
            List<ProductDto> soldProducts = productService.getSoldProductsBySeller(user.getId());
            double totalEarnings = soldProducts.stream()
                    .mapToDouble(ProductDto::getPrice)
                    .sum();
            int totalProductsSold = soldProducts.size();
            
            model.addAttribute("totalEarnings", totalEarnings);
            model.addAttribute("totalProductsSold", totalProductsSold);
        } catch (Exception e) {
            model.addAttribute("totalEarnings", 0.0);
            model.addAttribute("totalProductsSold", 0);
        }
        
        
        try {
            List<PurchaseDto> boughtProducts = productService.getBoughtProductsByBuyer(user.getId());
            double totalAmountSpent = boughtProducts.stream()
                    .mapToDouble(purchase -> purchase.getTotalPrice().doubleValue())
                    .sum();
            int totalProductsBought = boughtProducts.size();
            
            model.addAttribute("totalAmountSpent", totalAmountSpent);
            model.addAttribute("totalProductsBought", totalProductsBought);
        } catch (Exception e) {
            model.addAttribute("totalAmountSpent", 0.0);
            model.addAttribute("totalProductsBought", 0);
        }
        
        
        try {
            
            long completedChallengesCount = challengeService.getCompletedChallengesCount(user.getId());
            model.addAttribute("completedChallengesCount", completedChallengesCount);
            
            
            long currentStreak = challengeService.getCurrentStreak(user.getId());
            model.addAttribute("currentStreak", currentStreak);
        } catch (Exception e) {
            model.addAttribute("completedChallengesCount", 0);
            model.addAttribute("currentStreak", 0);
        }
        
        
        try {
            int userRank = userService.getUserRank(user.getId());
            model.addAttribute("userRank", userRank);
        } catch (Exception e) {
            model.addAttribute("userRank", 0);
        }
        
            
        try {
            List<PointEvent> recentEvents = pointsService.getRecentEvents(user.getId(), 5); 
            model.addAttribute("recentEvents", recentEvents);
        } catch (Exception e) {
            model.addAttribute("recentEvents", new ArrayList<>());
        }
        
        return "dashboard";
    }
    
    @PostMapping("/upload-profile-picture")
    public String uploadProfilePicture(@RequestParam("profilePicture") MultipartFile profilePicture,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            User currentUser = userService.getCurrentUser(principal.getName());
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "User session expired. Please login again.");
                return "redirect:/dashboard";
            }

            if (profilePicture != null && !profilePicture.isEmpty()) {
                String fileName = profilePictureStorageService.storeFile(profilePicture);
                // Use the /files/profiles/ endpoint for serving profile pictures
                currentUser.setProfilePicture("/files/profiles/" + fileName);
                userService.updateProfile(currentUser);
                
                // Track the action for points
                challengeTrackingService.trackUserAction(currentUser.getId(), "PROFILE_PHOTO_UPLOADED", null);
                
                redirectAttributes.addFlashAttribute("success", "Profile picture updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Please select a valid image file.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload profile picture: " + e.getMessage());
        }
        
        return "redirect:/dashboard";
    }
}

