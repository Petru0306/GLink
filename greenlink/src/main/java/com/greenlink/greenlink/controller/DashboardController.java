package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    
    @Autowired
    private ProductService productService;

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
        
        return "dashboard";
    }
}
