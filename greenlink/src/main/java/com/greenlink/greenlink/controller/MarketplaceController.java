package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.Product.Category;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.ProductService;
import com.greenlink.greenlink.service.FileStorageService;
import com.greenlink.greenlink.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/marketplace")
public class MarketplaceController {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketplaceController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @GetMapping("/{branch}")
    public String showBranchMarketplace(
            @PathVariable String branch,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Boolean ecoFriendly,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {
        
        try {
            Product.Branch branchEnum = Product.Branch.valueOf(branch.toUpperCase());
            
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductDto> productsPage;
            
            // Get filtered products first
            productsPage = productService.getFilteredProducts(
                    branchEnum, category, ecoFriendly, search, minPrice, maxPrice, pageable);
            
            // Add products to model (will be updated with negotiated prices if user is authenticated)
            List<ProductDto> products = new ArrayList<>(productsPage.getContent());
            
            // Try to get current user for personalized prices
            try {
                User currentUser = userService.getCurrentUser();
                
                // Add favorite IDs if user is authenticated
                List<Long> favoriteIds = currentUser.getFavoriteProducts().stream()
                        .map(Product::getId)
                        .toList();
                model.addAttribute("favoriteIds", favoriteIds);
                
                // Update products with negotiated prices
                for (int i = 0; i < products.size(); i++) {
                    ProductDto product = products.get(i);
                    try {
                        products.set(i, productService.getProductById(product.getId(), currentUser));
                    } catch (Exception e) {
                        // Keep original product if error occurs
                    }
                }
                
            } catch (Exception ex) {
                // User not authenticated - use products without negotiated prices
            }
            
            model.addAttribute("products", products);
            model.addAttribute("currentPage", productsPage.getNumber());
            model.addAttribute("totalPages", productsPage.getTotalPages());
            model.addAttribute("totalItems", productsPage.getTotalElements());
            
            model.addAttribute("branch", branchEnum);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("selectedEcoFriendly", ecoFriendly);
            model.addAttribute("searchTerm", search);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);
            
            // Set branch title based on branch parameter
            String branchTitle;
            switch (branchEnum) {
                case VERDE -> branchTitle = "Marketplace Verde";
                case FOOD -> branchTitle = "Food Market";
                case ELECTRO -> branchTitle = "Electro & Fashion Market";
                default -> branchTitle = "Marketplace";
            }
            model.addAttribute("branchTitle", branchTitle);
            
            // Add categories for filter
            model.addAttribute("categories", Category.values());
            
            return "marketplace";
            
        } catch (IllegalArgumentException e) {
            return "redirect:/marketplace";
        }
    }

    @GetMapping("/{branch}/product/{id}")
    public String showProduct(@PathVariable String branch, @PathVariable Long id, Model model) {
        try {
            // Default to false for unauthenticated users
            model.addAttribute("isCurrentUserSeller", false);

            // Add product with negotiated price if user is authenticated
            try {
                User currentUser = userService.getCurrentUser();
                ProductDto product = productService.getProductById(id, currentUser);
                List<ProductDto> similarProducts = productService.getSimilarProducts(id, 4, currentUser); // Get 4 similar products with negotiated prices
                model.addAttribute("product", product);
                model.addAttribute("similarProducts", similarProducts);
                model.addAttribute("branch", branch);
                
                // Debug info
                logger.info("Product seller ID: {}", product.getSellerId());
                logger.info("Current user ID: {}", currentUser.getId());
                
                List<Long> favoriteIds = currentUser.getFavoriteProducts().stream()
                        .map(Product::getId)
                        .toList();
                model.addAttribute("favoriteIds", favoriteIds);
                
                // Add a flag indicating if the current user is the seller
                boolean isCurrentUserSeller = false;
                if (product.getSellerId() != null && currentUser.getId() != null) {
                    isCurrentUserSeller = product.getSellerId().equals(currentUser.getId());
                    logger.info("Current user is seller: {}", isCurrentUserSeller);
                }
                model.addAttribute("isCurrentUserSeller", isCurrentUserSeller);
                
                // Set branch title based on branch parameter
                String branchTitle;
                try {
                    Product.Branch branchEnum = Product.Branch.valueOf(branch.toUpperCase());
                    switch (branchEnum) {
                        case VERDE -> branchTitle = "Marketplace Verde";
                        case FOOD -> branchTitle = "Food Market";
                        case ELECTRO -> branchTitle = "Electro Market";
                        default -> branchTitle = "Marketplace";
                    }
                } catch (IllegalArgumentException e) {
                    branchTitle = "Marketplace";
                }
                model.addAttribute("branchTitle", branchTitle);
                
            } catch (Exception ex) {
                // User not authenticated; get product without negotiated price
                ProductDto product = productService.getProductById(id);
                List<ProductDto> similarProducts = productService.getSimilarProducts(id, 4);
                model.addAttribute("product", product);
                model.addAttribute("similarProducts", similarProducts);
                model.addAttribute("branch", branch);
                
                // Set branch title based on branch parameter
                String branchTitle;
                try {
                    Product.Branch branchEnum = Product.Branch.valueOf(branch.toUpperCase());
                    switch (branchEnum) {
                        case VERDE -> branchTitle = "Marketplace Verde";
                        case FOOD -> branchTitle = "Food Market";
                        case ELECTRO -> branchTitle = "Electro Market";
                        default -> branchTitle = "Marketplace";
                    }
                } catch (IllegalArgumentException e) {
                    branchTitle = "Marketplace";
                }
                model.addAttribute("branchTitle", branchTitle);
                
                logger.warn("User not authenticated or error getting user: {}", ex.getMessage());
            }

            return "product-details";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Produsul nu a fost găsit");
            return "redirect:/marketplace";
        }
    }

    @GetMapping("/product/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new ProductDto());
        return "marketplace/product-form";
    }
    
    @GetMapping("/sell")
    public String showSellForm(Model model, @RequestHeader(value = "Referer", required = false) String referer) {
        ProductDto product = new ProductDto();
        model.addAttribute("product", product);
        model.addAttribute("isSelling", true);
        
        // Store the referer for the cancel button
        if (referer != null) {
            // Extract the path from the full URL
            String path = referer;
            try {
                if (referer.contains("://")) {
                    path = new java.net.URL(referer).getPath();
                }
            } catch (Exception e) {
                // If URL parsing fails, use the full referer
                logger.warn("Failed to parse referer URL: {}", referer, e);
            }
            model.addAttribute("refererPath", path);
        } else {
            model.addAttribute("refererPath", "/dashboard");
        }
        
        return "marketplace/product-form";
    }

    @GetMapping("/product/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        try {
            ProductDto product = productService.getProductById(id);
            model.addAttribute("product", product);
            return "marketplace/product-form";
        } catch (RuntimeException e) {
            return "redirect:/marketplace";
        }
    }

    @PostMapping("/product/add")
    public String addProduct(@ModelAttribute ProductDto productDto,
                           @RequestParam("imageFile") MultipartFile imageFile,
                           RedirectAttributes redirectAttributes) {
        try {
        if (!imageFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(imageFile);
            productDto.setImageUrl("/files/products/" + fileName);
        }

        productService.addProduct(productDto);
            redirectAttributes.addFlashAttribute("success", "Produsul a fost adăugat cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la adăugarea produsului: " + e.getMessage());
        }
        return "redirect:/marketplace";
    }
    
    @PostMapping("/sell")
    public String sellProduct(@ModelAttribute ProductDto productDto,
                            @RequestParam("imageFile") MultipartFile imageFile,
                            @RequestParam(value = "refererPath", required = false) String refererPath,
                            RedirectAttributes redirectAttributes) {
        try {
            logger.info("Starting to process product listing with image: {}", 
                      !imageFile.isEmpty() ? imageFile.getOriginalFilename() : "no image");
                      
            if (!imageFile.isEmpty()) {
                String fileName = fileStorageService.storeFile(imageFile);
                logger.info("Image stored successfully with filename: {}", fileName);
                productDto.setImageUrl("/files/products/" + fileName);
                logger.info("Set image URL to: {}", productDto.getImageUrl());
            } else {
                logger.warn("No image file uploaded for product: {}", productDto.getName());
            }
            
            // Set current user as seller
            User currentUser = userService.getCurrentUser();
            productDto.setSellerId(currentUser.getId());
            productDto.setSellerName(currentUser.getFirstName() + " " + currentUser.getLastName());
            
            productService.addProduct(productDto);
            logger.info("Product successfully added with ID: {} and imageUrl: {}", 
                      productDto.getId(), productDto.getImageUrl());
            redirectAttributes.addFlashAttribute("success", "Produsul tău a fost listat cu succes în marketplace!");
        } catch (Exception e) {
            logger.error("Error adding product: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la listarea produsului: " + e.getMessage());
        }
        
        // Redirect to referer if available, otherwise to dashboard
        if (refererPath != null && !refererPath.isEmpty()) {
            // Check if refererPath is a valid path in our application
            if (refererPath.startsWith("/marketplace") || refererPath.equals("/dashboard")) {
                return "redirect:" + refererPath;
            }
        }
        
        return "redirect:/dashboard";
    }

    @PostMapping("/product/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute ProductDto productDto,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes redirectAttributes,
                              @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = fileStorageService.storeFile(imageFile);
                productDto.setImageUrl("/files/products/" + fileName);
            }

            productService.updateProduct(id, productDto);
            redirectAttributes.addFlashAttribute("success", "Produsul a fost actualizat cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la actualizarea produsului: " + e.getMessage());
        }
        
        // Check if the update request came from the my-products page
        if (referer != null && referer.contains("/marketplace/my-products")) {
            return "redirect:/marketplace/my-products";
        }
        
        return "redirect:/marketplace";
    }

    @PostMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Attempting to delete product with ID: {}", id);
        
        try {
            // Get the product to verify it exists
            ProductDto product = productService.getProductById(id);
            logger.info("Found product to delete: {} (ID: {})", product.getName(), id);
            
            // Delete the product
            productService.deleteProduct(id);
            logger.info("Product deleted successfully");
            
            redirectAttributes.addFlashAttribute("success", "Produsul a fost șters cu succes!");
        } catch (Exception e) {
            logger.error("Error deleting product with ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la ștergerea produsului: " + e.getMessage());
        }
        
        // Always redirect to my-products page
        return "redirect:/marketplace/my-products";
    }

    @GetMapping
    public String showMarketplaceSelector() {
        return "marketplace-selector";
    }
    
    @GetMapping("/my-products")
    public String showMyProducts(Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            List<ProductDto> userProducts = productService.getProductsBySeller(currentUser.getId());
            model.addAttribute("products", userProducts);
            model.addAttribute("isMyProducts", true);
            return "marketplace/my-products";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
}