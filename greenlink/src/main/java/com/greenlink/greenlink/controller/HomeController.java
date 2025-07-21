package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("welcomeMessage", "Bine ai venit la GreenLink!");
        
        // Fetch top 5 users for the epic leaderboard section
        List<User> topUsers = userService.getTopUsers(5);
        model.addAttribute("topUsers", topUsers);
        
        return "index"; // Returnează fișierul index.html din templates
    }
}
