package com.greenlink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/recycling")
public class RecyclingController {

    @GetMapping
    public String recyclingPage(Model model) {
        List<String> recyclingPoints = List.of(
                "Centru 1 - Locație 1",
                "Centru 2 - Locație 2",
                "Centru 3 - Locație 3"
        );
        model.addAttribute("recyclingPoints", recyclingPoints);
        return "recycling"; // Returnează recycling.html din templates
    }

    @GetMapping("/point/{pointId}")
    public String recyclingPointDetails(@PathVariable Long pointId, Model model) {
        model.addAttribute("pointName", "Centru 1");
        model.addAttribute("pointAddress", "Strada exemplu, nr. 10");
        model.addAttribute("materialsAccepted", List.of("Plastic", "Hârtie", "Metal"));
        return "recycling-point-details"; // Returnează recycling-point-details.html
    }
}
