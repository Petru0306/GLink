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
    public String showCalculator(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("pageTitle", "Calculator Amprentă de Carbon");
        
        // Track carbon calculator usage for challenges when user visits the page
        if (user != null) {
            try {
                challengeTrackingService.trackUserAction(user.getId(), "CARBON_CALCULATOR_USED", null);
                System.out.println("Successfully tracked carbon calculator visit for user: " + user.getId());
            } catch (Exception e) {
                System.err.println("Error tracking carbon calculator visit: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return "calculator";
    }

    @PostMapping("/calculate")
    public String calculateCarbonFootprint(@AuthenticationPrincipal User user) {
        if (user != null) {
            // Track carbon calculator usage for challenges
            try {
                challengeTrackingService.trackUserAction(user.getId(), "CARBON_CALCULATOR_USED", null);
                System.out.println("Successfully tracked carbon calculator usage for user: " + user.getId());
            } catch (Exception e) {
                System.err.println("Error tracking carbon calculator usage: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("User is not authenticated when calculating carbon footprint");
        }
        return "redirect:/calculator";
    }

    @GetMapping("/rezultat")
    public String showResult(Model model) {
        return "calculator";
    }
}