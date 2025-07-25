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
            
            // Get all bought products for statistics calculation
            List<PurchaseDto> allBoughtProducts = productService.getBoughtProductsByBuyer(currentUser.getId());

            // Calculate statistics from all products
            BigDecimal totalAmountSpent = BigDecimal.ZERO;
            int totalProductsBought = 0;
            LocalDateTime lastPurchaseDate = null;

            if (allBoughtProducts != null && !allBoughtProducts.isEmpty()) {
                totalProductsBought = allBoughtProducts.size();

                for (PurchaseDto purchase : allBoughtProducts) {
                    if (purchase != null && purchase.getTotalPrice() != null) {
                        totalAmountSpent = totalAmountSpent.add(purchase.getTotalPrice());
                    }

                    if (purchase != null && purchase.getPurchaseDate() != null) {
                        if (lastPurchaseDate == null || purchase.getPurchaseDate().isAfter(lastPurchaseDate)) {
                            lastPurchaseDate = purchase.getPurchaseDate();
                        }
                    }
                }
            }

            // Create pageable for pagination
            Pageable pageable = PageRequest.of(page, size);

            // Get paginated bought products
            Page<PurchaseDto> boughtProductsPage = productService.getBoughtProductsByBuyerPaginated(currentUser.getId(), pageable);

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

            return "dashboard/buy-history";
        } catch (Exception e) {
            System.err.println("ERROR in showBuyHistory: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login";
        }
    }
}
