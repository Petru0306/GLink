package com.greenlink.greenlink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/confidentialitate")
public class PrivacyController {
    // Explicitly allow access without authentication
    
    @GetMapping
    public String showPrivacyPage(Model model) {
        model.addAttribute("pageTitle", "Politica de Confidențialitate GreenLink");
        return "confidentialitate"; // va căuta template-ul confidentialitate.html
    }
}
