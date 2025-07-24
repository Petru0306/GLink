package com.greenlink.greenlink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/termeni")
public class TermsController {
    // Explicitly allow access without authentication

    @GetMapping
    public String showTermsPage(Model model) {
        model.addAttribute("pageTitle", "Termeni și Condiții GreenLink");
        return "termeni"; // va căuta template-ul termeni.html
    }
}
