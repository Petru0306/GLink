package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.ChallengeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/provocari")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    public String showChallenges(Model model, @AuthenticationPrincipal User user) {
        if (user != null) {
            model.addAttribute("activeChallenges", challengeService.getUserActiveChallenges(user.getId()));
            model.addAttribute("completedChallenges", challengeService.getUserCompletedChallenges(user.getId()));
            model.addAttribute("totalPoints", challengeService.getTotalPoints(user.getId()));
            model.addAttribute("completedCount", challengeService.getCompletedChallengesCount(user.getId()));
            model.addAttribute("currentStreak", challengeService.getCurrentStreak(user.getId()));
            model.addAttribute("allChallenges", challengeService.getAllChallenges());
        }
        return "provocari";
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

    @GetMapping("/type/{type}")
    public String getChallengesByType(@PathVariable Challenge.ChallengeType type, Model model) {
        model.addAttribute("challenges", challengeService.getChallengesByType(type));
        return "provocari :: challengeList";
    }
} 