package com.greenlink.greenlink.controller;
import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.ChallengeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@Controller
@RequestMapping("/provocari")
public class ChallengeController {

    private static final Logger logger = Logger.getLogger(ChallengeController.class.getName());
    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    public String showChallenges(Model model, @AuthenticationPrincipal User user) {
        try {
            if (user != null) {
                logger.info("Loading challenges for user: " + user.getId());
                
                // Initialize user challenges if they don't exist
                challengeService.initializeUserChallenges(user.getId());
                
                model.addAttribute("userChallengesByCategory", challengeService.getUserChallengesByCategory(user.getId()));
                model.addAttribute("totalPoints", challengeService.getTotalPoints(user.getId()));
                model.addAttribute("completedCount", challengeService.getCompletedChallengesCount(user.getId()));
                model.addAttribute("currentStreak", challengeService.getCurrentStreak(user.getId()));
                model.addAttribute("categories", Challenge.ChallengeCategory.values());
                
                logger.info("Successfully loaded challenges for user: " + user.getId());
            } else {
                logger.info("No authenticated user, showing empty challenges page");
            }
            return "provocari";
        } catch (Exception e) {
            logger.severe("Error loading challenges: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load challenges: " + e.getMessage());
            return "provocari";
        }
    }

    @PostMapping("/{challengeId}/start")
    @ResponseBody
    public String startChallenge(@PathVariable Long challengeId, @AuthenticationPrincipal User user) {
        try {
            challengeService.startChallenge(user.getId(), challengeId);
            return "success";
        } catch (Exception e) {
            logger.severe("Error starting challenge: " + e.getMessage());
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/{challengeId}/progress")
    @ResponseBody
    public String updateProgress(
            @PathVariable Long challengeId,
            @RequestParam int progress,
            @AuthenticationPrincipal User user) {
        try {
            challengeService.updateProgress(user.getId(), challengeId, progress);
            return "success";
        } catch (Exception e) {
            logger.severe("Error updating progress: " + e.getMessage());
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/category/{category}")
    public String getChallengesByCategory(@PathVariable Challenge.ChallengeCategory category, Model model, @AuthenticationPrincipal User user) {
        try {
            if (user != null) {
                model.addAttribute("category", category);
                model.addAttribute("userChallenges", challengeService.getUserChallengesByStatus(user.getId(), null));
            }
            return "provocari :: challengeList";
        } catch (Exception e) {
            logger.severe("Error loading challenges by category: " + e.getMessage());
            return "provocari :: challengeList";
        }
    }

    @PostMapping("/event/{event}")
    @ResponseBody
    public String triggerEvent(@PathVariable String event, @RequestParam(defaultValue = "1") int increment, @AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                return "error: User not authenticated";
            }
            challengeService.updateProgressByEvent(user.getId(), event, increment);
            return "success";
        } catch (Exception e) {
            logger.severe("Error triggering event: " + e.getMessage());
            return "error: " + e.getMessage();
        }
    }
} 