package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.model.PointEvent;
import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.dto.PurchaseDto;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ProductService;
import com.greenlink.greenlink.service.ChallengeService;
import com.greenlink.greenlink.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    public DashboardController(UserService userService) { 
        this.userService = userService;
    }

    @GetMapping
    public String showDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        // Load fresh user data from database to get updated points
        User user = userService.getCurrentUser(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        
        // Add sales statistics for Quick Actions
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
        
        // Add buy history statistics for Quick Actions
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
        
        // Add challenge statistics
        try {
            // Get completed challenges count
            long completedChallengesCount = challengeService.getCompletedChallengesCount(user.getId());
            model.addAttribute("completedChallengesCount", completedChallengesCount);
            
            // Get current streak
            long currentStreak = challengeService.getCurrentStreak(user.getId());
            model.addAttribute("currentStreak", currentStreak);
        } catch (Exception e) {
            model.addAttribute("completedChallengesCount", 0);
            model.addAttribute("currentStreak", 0);
        }
        
        // Add recent events for Recent Activity section
        try {
            List<PointEvent> recentEvents = pointsService.getRecentEvents(user.getId(), 5); // Last 5 events
            model.addAttribute("recentEvents", recentEvents);
        } catch (Exception e) {
            model.addAttribute("recentEvents", new ArrayList<>());
        }
        
        return "dashboard";
    }
}

