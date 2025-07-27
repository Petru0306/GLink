package com.greenlink.greenlink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/informatii")
public class InfoController {

    @GetMapping
    public String showInfo(Model model) {
        model.addAttribute("pageTitle", "Informații - GreenLink");
        return "informatii";
    }
} 