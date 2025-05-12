package com.greenlink.greenlink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/despre")
public class AboutController {

    @GetMapping
    public String showAboutPage(Model model) {
        // Poți adăuga atribute în model dacă ai nevoie să trimiți date către view
        model.addAttribute("pageTitle", "Despre GreenLink");
        return "despre"; // va căuta template-ul despre.html
    }

    @GetMapping("/echipa")
    public String showTeam(Model model) {
        model.addAttribute("pageTitle", "Echipa GreenLink");
        return "despre-echipa";
    }

    @GetMapping("/misiune")
    public String showMission(Model model) {
        model.addAttribute("pageTitle", "Misiunea GreenLink");
        return "despre-misiune";
    }
}