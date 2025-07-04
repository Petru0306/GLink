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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String showMarketplace(
            @PathVariable(required = false) String branch,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Boolean ecoFriendly,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sort) {

        Product.Branch selectedBranch;
        try {
            selectedBranch = branch == null ? Product.Branch.VERDE : Product.Branch.valueOf(branch.toUpperCase());
        } catch (IllegalArgumentException ex) {
            selectedBranch = Product.Branch.VERDE;
        }

        System.out.println("Entering showMarketplace method");

        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
            System.out.println("Current user fetched via UserService: " + currentUser.getEmail());
        } catch (Exception ex) {
            System.out.println("No authenticated user.");
        }

        Sort sorting = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sort != null) {
            switch (sort) {
                case "price_asc":
                    sorting = Sort.by(Sort.Direction.ASC, "price");
                    break;
                case "price_desc":
                    sorting = Sort.by(Sort.Direction.DESC, "price");
                    break;
                case "newest":
                    sorting = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }
        }

        PageRequest pageRequest = PageRequest.of(page, size, sorting);
        Page<ProductDto> products;

        // Apply all filters together
        products = productService.getFilteredProducts(
            selectedBranch,
            category,
            ecoFriendly,
            search,
            minPrice,
            maxPrice,
            pageRequest
        );

        System.out.println("Products fetched: " + products.getTotalElements());

        // Add user and favorites to model if authenticated
        if (currentUser != null) {
            try {
                System.out.println("Adding user favorites to model");
                List<Long> favoriteIds = currentUser.getFavoriteProducts().stream()
                    .map(Product::getId)
                    .toList();
                System.out.println("User favorite IDs: " + favoriteIds);
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("favoriteIds", favoriteIds);
            } catch (Exception e) {
                System.out.println("Error getting user favorites: " + e.getMessage());
                e.printStackTrace();
            }
        }

        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("categories", Category.values());
        model.addAttribute("branch", selectedBranch.name().toLowerCase());

        String branchTitle;
        switch (selectedBranch) {
            case VERDE -> branchTitle = "Marketplace Verde";
            case FOOD -> branchTitle = "Food Market";
            case ELECTRO -> branchTitle = "Electro & Fashion";
            default -> branchTitle = "Marketplace";
        }
        model.addAttribute("branchTitle", branchTitle);

        System.out.println("Model attributes added, returning marketplace template");
        return "marketplace";
    }

    @GetMapping("/{branch}/product/{id}")
    public String showProduct(@PathVariable String branch, @PathVariable Long id, Model model) {
        try {
            ProductDto product = productService.getProductById(id);
            List<ProductDto> similarProducts = productService.getSimilarProducts(id, 4); // Get 4 similar products
            model.addAttribute("product", product);
            model.addAttribute("similarProducts", similarProducts);
            model.addAttribute("branch", branch);

            // Add favourite IDs if user is authenticated
            try {
                User currentUser = userService.getCurrentUser();
                List<Long> favoriteIds = currentUser.getFavoriteProducts().stream()
                        .map(Product::getId)
                        .toList();
                model.addAttribute("favoriteIds", favoriteIds);
            } catch (Exception ex) {
                // User not authenticated; favouriteIds remains null
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
            if (!imageFile.isEmpty()) {
                String fileName = fileStorageService.storeFile(imageFile);
                productDto.setImageUrl("/files/products/" + fileName);
            }
            
            // Set current user as seller
            User currentUser = userService.getCurrentUser();
            productDto.setSellerId(currentUser.getId());
            productDto.setSellerName(currentUser.getFirstName() + " " + currentUser.getLastName());
            
            productService.addProduct(productDto);
            redirectAttributes.addFlashAttribute("success", "Produsul tău a fost listat cu succes în marketplace!");
        } catch (Exception e) {
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