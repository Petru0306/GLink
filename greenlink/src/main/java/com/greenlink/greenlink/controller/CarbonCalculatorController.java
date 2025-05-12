package com.greenlink.greenlink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/calculator")
public class CarbonCalculatorController {

    @GetMapping
    public String showCalculator(Model model) {
        model.addAttribute("pageTitle", "Calculator Amprentă de Carbon");
        return "calculator";
    }

    @GetMapping("/rezultat")
    public String showResult(Model model) {
        return "calculator";
    }
}