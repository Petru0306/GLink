package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.PurchaseDto;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    public String showBuyHistory(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "6") int size) {
        try {
            User currentUser = userService.getCurrentUser();
            System.out.println("=== BUY HISTORY CONTROLLER START ===");
            System.out.println("Current user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");
            
            // Get all bought products for statistics calculation
            List<PurchaseDto> allBoughtProducts = productService.getBoughtProductsByBuyer(currentUser.getId());
            System.out.println("Raw bought products from service: " + (allBoughtProducts != null ? allBoughtProducts.size() : 0));

            // Calculate statistics from all products
            BigDecimal totalAmountSpent = BigDecimal.ZERO;
            int totalProductsBought = 0;
            LocalDateTime lastPurchaseDate = null;

            System.out.println("=== CALCULATING BUY HISTORY STATISTICS ===");
            System.out.println("Total bought products found: " + (allBoughtProducts != null ? allBoughtProducts.size() : 0));

            if (allBoughtProducts != null && !allBoughtProducts.isEmpty()) {
                totalProductsBought = allBoughtProducts.size();

                for (PurchaseDto purchase : allBoughtProducts) {
                    System.out.println("Processing purchase: " + (purchase != null ? purchase.getProduct().getName() : "null"));
                    
                    if (purchase != null && purchase.getTotalPrice() != null) {
                        System.out.println("  - Price: " + purchase.getTotalPrice() + " RON");
                        totalAmountSpent = totalAmountSpent.add(purchase.getTotalPrice());
                    } else {
                        System.out.println("  - Price: null or purchase is null");
                    }

                    if (purchase != null && purchase.getPurchaseDate() != null) {
                        System.out.println("  - Purchase date: " + purchase.getPurchaseDate());
                        if (lastPurchaseDate == null || purchase.getPurchaseDate().isAfter(lastPurchaseDate)) {
                            lastPurchaseDate = purchase.getPurchaseDate();
                            System.out.println("  - New latest purchase date: " + lastPurchaseDate);
                        }
                    } else {
                        System.out.println("  - Purchase date: null");
                    }
                }
            }

            System.out.println("Final statistics:");
            System.out.println("  - Total products bought: " + totalProductsBought);
            System.out.println("  - Total amount spent: " + totalAmountSpent + " RON");
            System.out.println("  - Last purchase date: " + lastPurchaseDate);
            System.out.println("=== END STATISTICS CALCULATION ===");

            // Create pageable for pagination
            Pageable pageable = PageRequest.of(page, size);

            // Get paginated bought products
            Page<PurchaseDto> boughtProductsPage = productService.getBoughtProductsByBuyerPaginated(currentUser.getId(), pageable);
            System.out.println("Paginated products: " + boughtProductsPage.getContent().size() + " out of " + boughtProductsPage.getTotalElements());

            model.addAttribute("buyHistory", boughtProductsPage.getContent());
            model.addAttribute("totalAmountSpent", totalAmountSpent);
            model.addAttribute("totalProductsBought", totalProductsBought);
            model.addAttribute("lastPurchaseDate", lastPurchaseDate);
            model.addAttribute("currentUser", currentUser);

            // Pagination attributes
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", boughtProductsPage.getTotalPages());
            model.addAttribute("totalElements", boughtProductsPage.getTotalElements());
            model.addAttribute("hasNext", boughtProductsPage.hasNext());
            model.addAttribute("hasPrevious", boughtProductsPage.hasPrevious());
            model.addAttribute("showPagination", totalProductsBought > 6);

            System.out.println("=== MODEL ATTRIBUTES SET ===");
            System.out.println("buyHistory size: " + boughtProductsPage.getContent().size());
            System.out.println("totalAmountSpent: " + totalAmountSpent);
            System.out.println("totalProductsBought: " + totalProductsBought);
            System.out.println("lastPurchaseDate: " + lastPurchaseDate);
            System.out.println("=== BUY HISTORY CONTROLLER END ===");

            return "dashboard/buy-history";
        } catch (Exception e) {
            System.err.println("ERROR in showBuyHistory: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login";
        }
    }
}
