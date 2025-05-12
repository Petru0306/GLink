package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.model.Product.Category;
import com.greenlink.greenlink.service.ProductService;
import com.greenlink.greenlink.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/marketplace")
public class MarketplaceController {

    @Autowired
    private ProductService productService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public String showMarketplace(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Boolean ecoFriendly,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort) {

        Sort sorting = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sort != null) {
            switch (sort) {
                case "price_asc":
                    sorting = Sort.by(Sort.Direction.ASC, "price");
                    break;
                case "price_desc":
                    sorting = Sort.by(Sort.Direction.DESC, "price");
                    break;
            }
        }

        PageRequest pageRequest = PageRequest.of(page, size, sorting);
        Page<ProductDto> products;

        if (category != null) {
            products = productService.getProductsByCategory(category, pageRequest);
        } else if (ecoFriendly != null && ecoFriendly) {
            products = productService.getEcoFriendlyProducts(pageRequest);
        } else if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search, pageRequest);
        } else {
            products = productService.getProducts(pageRequest);
        }

        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("categories", Category.values());

        return "marketplace";
    }

    @GetMapping("/product/{id}")
    public String showProduct(@PathVariable Long id, Model model) {
        try {
            ProductDto product = productService.getProductById(id);
            model.addAttribute("product", product);
            return "product-details";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Produsul nu a fost găsit");
            return "redirect:/marketplace";
        }
    }
// nu are rost /add, /update...
    @PostMapping("/product/add")
    public String addProduct(@ModelAttribute ProductDto productDto,
                             @RequestParam("imageFile") MultipartFile imageFile) {
        if (!imageFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(imageFile);
            productDto.setImageUrl("/uploads/products/" + fileName);
        }

        productService.addProduct(productDto);
        return "redirect:/marketplace";
    }

    @PostMapping("/product/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute ProductDto productDto,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(imageFile);
            productDto.setImageUrl("/uploads/products/" + fileName);
        }

        productService.updateProduct(id, productDto);
        return "redirect:/marketplace";
    }
}