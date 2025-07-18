package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) { 
        this.userService = userService;
    }

    @GetMapping
    public String showDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        // Load fresh user data from database to get updated points
        User user = userService.getCurrentUser(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        return "dashboard";
    }
}
