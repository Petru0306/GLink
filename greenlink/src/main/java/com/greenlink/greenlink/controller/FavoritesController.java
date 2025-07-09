package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.ProductRepository;
import com.greenlink.greenlink.repository.UserRepository;
import com.greenlink.greenlink.service.ProductService;
import com.greenlink.greenlink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/marketplace/favorites")
public class FavoritesController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductService productService;

    /**
     * Shows the list of favourite products for the currently authenticated user.
     */
    @GetMapping
    @Transactional(readOnly = true)
    public String showFavorites(Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Check if user is authenticated and not anonymous
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            
            // Check if it's an AJAX request
            String requestedWith = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith)) {
                // Return JSON response for AJAX requests
                return "redirect:/marketplace?showLogin=true";
            }
            
            // For regular requests, redirect to marketplace with a parameter to show login modal
            return "redirect:/marketplace?showLogin=true";
        }

        try {
            User currentUser = userService.getCurrentUser();
            List<Product> favoriteProducts = currentUser.getFavoriteProducts();
            
            // Convert to DTOs with negotiated prices
            List<ProductDto> favoriteProductDtos = favoriteProducts.stream()
                .map(product -> productService.getProductById(product.getId(), currentUser))
                .collect(Collectors.toList());
            
            model.addAttribute("products", favoriteProductDtos);
            return "favorites";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "A apărut o eroare la încărcarea listei de favorite. Te rugăm să încerci din nou.");
            return "redirect:/marketplace";
        }
    }

    /**
     * Adds a product to the authenticated user's favourites.
     */
    @PostMapping("/add/{productId}")
    @ResponseBody
    public ResponseEntity<?> addFavorite(@PathVariable Long productId, HttpServletRequest request) {
        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Trebuie să fii autentificat pentru a adăuga produse la favorite");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            User currentUser = userService.getCurrentUser();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));
            
            boolean wasAdded = false;
            if (!currentUser.getFavoriteProducts().contains(product)) {
                currentUser.getFavoriteProducts().add(product);
                userRepository.save(currentUser);
                wasAdded = true;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("added", wasAdded);
            response.put("message", wasAdded ? "Produs adăugat la favorite" : "Produsul era deja la favorite");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare la adăugarea produsului la favorite: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Removes a product from the authenticated user's favourites.
     */
    @PostMapping("/remove/{productId}")
    @ResponseBody
    public ResponseEntity<?> removeFavorite(@PathVariable Long productId, HttpServletRequest request) {
        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Trebuie să fii autentificat pentru a elimina produse de la favorite");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            User currentUser = userService.getCurrentUser();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));
            
            boolean wasRemoved = false;
            if (currentUser.getFavoriteProducts().contains(product)) {
                currentUser.getFavoriteProducts().remove(product);
                userRepository.save(currentUser);
                wasRemoved = true;
            }
            
            // Check if it's an AJAX request
            String requestedWith = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", wasRemoved);
                response.put("message", wasRemoved ? "Produs eliminat de la favorite" : "Produsul nu era la favorite");
                return ResponseEntity.ok(response);
            } else {
                // Regular form submission - redirect back to favorites page
                return ResponseEntity.ok()
                    .header("Location", "/marketplace/favorites")
                    .build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare la eliminarea produsului de la favorite: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 