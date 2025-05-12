package com.greenlink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/marketplace")
public class MarketplaceController {

    @GetMapping
    public String marketplacePage(Model model) {
        List<String> products = List.of("Produs 1", "Produs 2", "Produs 3");
        model.addAttribute("products", products);
        return "marketplace"; // Returnează marketplace.html din templates
    }

    @GetMapping("/{productId}")
    public String productDetails(@PathVariable Long productId, Model model) {
        model.addAttribute("productName", "Produs exemplu");
        model.addAttribute("productDescription", "Detalii despre produsul exemplu.");
        model.addAttribute("price", 100.0);
        return "product-details"; // Returnează product-details.html
    }
}
