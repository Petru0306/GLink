package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("welcomeMessage", "Bine ai venit la GreenLink!");
        
        // Fetch top 5 users for the epic leaderboard section
        List<User> topUsers = userService.getTopUsers(5);
        model.addAttribute("topUsers", topUsers);
        
        // Fetch total statistics for the epic leaderboard section
        long totalCompetitors = userService.getTotalActiveUserCount();
        long totalPoints = userService.getTotalPoints();
        long totalLevels = userService.getTotalLevels();
        
        model.addAttribute("totalCompetitors", totalCompetitors);
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("totalLevels", totalLevels);
        
        // Fetch featured marketplace products for the Marketplace Explorer section
        List<ProductDto> featuredProducts = productService.getAllProducts().stream()
                .limit(5) // Show 5 products in the preview
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("featuredProducts", featuredProducts);
        
        return "index"; // Returnează fișierul index.html din templates
    }
}
