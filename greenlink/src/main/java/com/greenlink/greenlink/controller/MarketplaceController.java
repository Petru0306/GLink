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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
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
                case "newest":
                    sorting = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }
        }

        PageRequest pageRequest = PageRequest.of(page, size, sorting);
        Page<ProductDto> products;

        // Apply all filters together
        products = productService.getFilteredProducts(
            category,
            ecoFriendly,
            search,
            minPrice,
            maxPrice,
            pageRequest
        );

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
            List<ProductDto> similarProducts = productService.getSimilarProducts(id, 4); // Get 4 similar products
            model.addAttribute("product", product);
            model.addAttribute("similarProducts", similarProducts);
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
            productDto.setImageUrl("/uploads/products/" + fileName);
        }

        productService.addProduct(productDto);
            redirectAttributes.addFlashAttribute("success", "Produsul a fost adăugat cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la adăugarea produsului: " + e.getMessage());
        }
        return "redirect:/marketplace";
    }

    @PostMapping("/product/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute ProductDto productDto,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) {
        try {
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(imageFile);
            productDto.setImageUrl("/uploads/products/" + fileName);
        }

        productService.updateProduct(id, productDto);
            redirectAttributes.addFlashAttribute("success", "Produsul a fost actualizat cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la actualizarea produsului: " + e.getMessage());
        }
        return "redirect:/marketplace";
    }

    @PostMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Produsul a fost șters cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la ștergerea produsului: " + e.getMessage());
        }
        return "redirect:/marketplace";
    }
}