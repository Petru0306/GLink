package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.ChallengeTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/calculator")
public class CarbonCalculatorController {

    @Autowired
    private ChallengeTrackingService challengeTrackingService;

    @GetMapping
    public String showCalculator(Model model) {
        model.addAttribute("pageTitle", "Calculator Amprentă de Carbon");
        return "calculator";
    }

    @PostMapping("/calculate")
    public String calculateCarbonFootprint(@AuthenticationPrincipal User user) {
        if (user != null) {
            // Track carbon calculator usage for challenges
            challengeTrackingService.trackUserAction(user.getId(), "CARBON_CALCULATOR_USED", null);
        }
        return "redirect:/calculator";
    }

    @GetMapping("/rezultat")
    public String showResult(Model model) {
        return "calculator";
    }
}