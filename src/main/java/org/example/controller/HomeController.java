package com.greenlink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("welcomeMessage", "Bine ai venit la GreenLink!");
        return "index"; // Returnează index.html din templates
    }
}
