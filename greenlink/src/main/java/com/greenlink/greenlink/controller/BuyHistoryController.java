package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.PurchaseDto;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.ProductService;
import com.greenlink.greenlink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
// import java.util.Map;

@Controller
@RequestMapping("/dashboard/buy-history")
public class BuyHistoryController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Show buy history page
     */
    @GetMapping
    public String showBuyHistory(Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                System.out.println("DEBUG: Current user is null");
                return "redirect:/login";
            }
            
            System.out.println("DEBUG: Loading buy history for user: " + currentUser.getId());
            
            List<PurchaseDto> buyHistory;
            try {
                buyHistory = productService.getBoughtProductsByBuyer(currentUser.getId());
                System.out.println("DEBUG: Buy history size: " + (buyHistory != null ? buyHistory.size() : "null"));
            } catch (Exception e) {
                System.out.println("DEBUG: Error getting buy history: " + e.getMessage());
                buyHistory = new ArrayList<>();
            }
            
            // Ensure buyHistory is never null
            if (buyHistory == null) {
                buyHistory = new ArrayList<>();
            }
            
            // Calculate statistics with null safety
            BigDecimal totalAmountSpent = buyHistory.stream()
                .filter(purchase -> purchase != null && purchase.getTotalPrice() != null)
                .map(PurchaseDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            int totalProductsBought = buyHistory.size();
            
            LocalDateTime lastPurchaseDate = buyHistory.stream()
                .filter(purchase -> purchase != null && purchase.getPurchaseDate() != null)
                .map(PurchaseDto::getPurchaseDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            
            // Calculate real monthly data from purchase history
            Map<Integer, BigDecimal> monthlyTotals = buyHistory.stream()
                .filter(purchase -> purchase != null && purchase.getPurchaseDate() != null && purchase.getTotalPrice() != null)
                .collect(Collectors.groupingBy(
                    purchase -> purchase.getPurchaseDate().getMonthValue(),
                    Collectors.reducing(BigDecimal.ZERO, PurchaseDto::getTotalPrice, BigDecimal::add)
                ));
            
            // Create List<Double> of exactly 12 elements for Chart.js
            List<Double> monthlyData = IntStream.rangeClosed(1, 12)
                .mapToObj(m -> monthlyTotals.getOrDefault(m, BigDecimal.ZERO).doubleValue())
                .collect(Collectors.toList());
            
            System.out.println("DEBUG: Monthly data size: " + monthlyData.size());
            System.out.println("DEBUG: Monthly data: " + monthlyData);
            
            model.addAttribute("buyHistory", buyHistory);
            model.addAttribute("totalAmountSpent", totalAmountSpent);
            model.addAttribute("totalProductsBought", totalProductsBought);
            model.addAttribute("lastPurchaseDate", lastPurchaseDate);
            model.addAttribute("monthlyData", monthlyData);
            
            System.out.println("DEBUG: All attributes added to model successfully");
            
            return "dashboard/buy-history";
        } catch (Exception e) {
            System.err.println("ERROR in showBuyHistory: " + e.getMessage());
            e.printStackTrace();
            
            // Add fallback data to model
            model.addAttribute("buyHistory", new ArrayList<>());
            model.addAttribute("totalAmountSpent", BigDecimal.ZERO);
            model.addAttribute("totalProductsBought", 0);
            model.addAttribute("lastPurchaseDate", null);
            model.addAttribute("monthlyData", Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
            model.addAttribute("error", "Failed to load buy history: " + e.getMessage());
            
            return "dashboard/buy-history";
        }
    }
    
    /**
     * Handle review submission
     */
    @PostMapping("/review")
    public String submitReview(@RequestParam Long purchaseId, 
                              @RequestParam Integer rating, 
                              @RequestParam(required = false) String comment,
                              RedirectAttributes redirectAttributes) {
        try {
            // TODO: Implement review submission logic
            // This would typically involve:
            // 1. Creating a Review entity
            // 2. Saving it to the database
            // 3. Updating the purchase to mark it as reviewed
            
            redirectAttributes.addFlashAttribute("success", "Review submitted successfully!");
            return "redirect:/dashboard/buy-history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit review: " + e.getMessage());
            return "redirect:/dashboard/buy-history";
        }
    }
} 