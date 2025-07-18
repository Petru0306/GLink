package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.ProductService;
import com.greenlink.greenlink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dashboard/sales")
public class SalesController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Show sales history page
     */
    @GetMapping
    public String showSalesHistory(Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            List<ProductDto> soldProducts = productService.getSoldProductsBySeller(currentUser.getId());
            
            // Calculate statistics
            double totalEarnings = soldProducts.stream()
                    .mapToDouble(ProductDto::getPrice)
                    .sum();
            
            int totalProductsSold = soldProducts.size();
            
            model.addAttribute("soldProducts", soldProducts);
            model.addAttribute("totalEarnings", totalEarnings);
            model.addAttribute("totalProductsSold", totalProductsSold);
            model.addAttribute("currentUser", currentUser);
            
            return "dashboard/sales-history";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
} 