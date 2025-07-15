package com.greenlink.greenlink.controller;
import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.ChallengeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;
import java.util.logging.Level;

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
            logger.info("Accessing challenges page for user: " + (user != null ? user.getEmail() : "anonymous"));
            
            if (user != null) {
                // Initialize user challenges if they don't exist
                logger.info("Initializing user challenges for user ID: " + user.getId());
                challengeService.initializeUserChallenges(user.getId());
                
                logger.info("Getting user challenges by category for user ID: " + user.getId());
                model.addAttribute("userChallengesByCategory", challengeService.getUserChallengesByCategory(user.getId()));
                
                logger.info("Getting total points for user ID: " + user.getId());
                model.addAttribute("totalPoints", challengeService.getTotalPoints(user.getId()));
                
                logger.info("Getting completed count for user ID: " + user.getId());
                model.addAttribute("completedCount", challengeService.getCompletedChallengesCount(user.getId()));
                
                logger.info("Getting current streak for user ID: " + user.getId());
                model.addAttribute("currentStreak", challengeService.getCurrentStreak(user.getId()));
                
                model.addAttribute("categories", Challenge.ChallengeCategory.values());
                logger.info("Successfully loaded challenges page for user: " + user.getEmail());
            } else {
                logger.info("User is not authenticated, showing empty challenges page");
            }
            return "provocari";
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading challenges page", e);
            model.addAttribute("error", "Failed to load challenges: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/{challengeId}/start")
    @ResponseBody
    public String startChallenge(@PathVariable Long challengeId, @AuthenticationPrincipal User user) {
        try {
            challengeService.startChallenge(user.getId(), challengeId);
            return "success";
        } catch (Exception e) {
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
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/category/{category}")
    public String getChallengesByCategory(@PathVariable Challenge.ChallengeCategory category, Model model, @AuthenticationPrincipal User user) {
        if (user != null) {
            model.addAttribute("category", category);
            model.addAttribute("userChallenges", challengeService.getUserChallengesByStatus(user.getId(), null));
        }
        return "provocari :: challengeList";
    }

    @GetMapping("/test")
    @ResponseBody
    public String testChallenges() {
        try {
            long challengeCount = challengeService.getAllChallenges().size();
            return "Database is working. Found " + challengeCount + " challenges.";
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database test failed", e);
            return "Database test failed: " + e.getMessage();
        }
    }

    @PostMapping("/event/{event}")
    @ResponseBody
    public String triggerEvent(@PathVariable String event, @RequestParam(defaultValue = "1") int increment, @AuthenticationPrincipal User user) {
        try {
            challengeService.updateProgressByEvent(user.getId(), event, increment);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
} 