package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.ProductService;
import com.greenlink.greenlink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/dashboard/sales")
public class SalesController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    
    @GetMapping
    public String showSalesHistory(Model model, 
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "6") int size) {
        try {
            User currentUser = userService.getCurrentUser();
            
            
            List<ProductDto> allSoldProducts = productService.getSoldProductsBySeller(currentUser.getId());
            
            
            double totalEarnings = allSoldProducts.stream()
                    .mapToDouble(ProductDto::getPrice)
                    .sum();
            
            int totalProductsSold = allSoldProducts.size();
            
            
            Pageable pageable = PageRequest.of(page, size);
            
            
            Page<ProductDto> soldProductsPage = productService.getSoldProductsBySellerPaginated(currentUser.getId(), pageable);
            
            model.addAttribute("soldProducts", soldProductsPage.getContent());
            model.addAttribute("totalEarnings", totalEarnings);
            model.addAttribute("totalProductsSold", totalProductsSold);
            model.addAttribute("currentUser", currentUser);
            
                
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", soldProductsPage.getTotalPages());
            model.addAttribute("totalElements", soldProductsPage.getTotalElements());
            model.addAttribute("hasNext", soldProductsPage.hasNext());
            model.addAttribute("hasPrevious", soldProductsPage.hasPrevious());
            model.addAttribute("showPagination", totalProductsSold > 6);
            
            return "dashboard/sales-history";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
} 