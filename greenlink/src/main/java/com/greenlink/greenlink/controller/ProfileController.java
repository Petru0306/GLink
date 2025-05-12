package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.QuizResult;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ChallengeService;
import com.greenlink.greenlink.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private QuizService quizService;

    @GetMapping
    public String showProfile(Model model, Principal principal) {
        User user = userService.getCurrentUser(principal.getName());

        // Add user data to model
        model.addAttribute("user", user);
        model.addAttribute("totalPoints", user.getPoints());

        // Get quiz results
        List<QuizResult> quizResults = quizService.getUserQuizResults(user.getId());
        model.addAttribute("quizResults", quizResults);

        // Get challenges
        List<Challenge> activeChallenges = challengeService.getUserActiveChallenges(user.getId());
        List<Challenge> completedChallenges = challengeService.getUserCompletedChallenges(user.getId());
        model.addAttribute("activeChallenges", activeChallenges);
        model.addAttribute("completedChallenges", completedChallenges);

        return "profil";
    }

    @GetMapping("/edit")
    public String showEditProfile(Model model, Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        model.addAttribute("user", user);
        return "edit-profile";
    }

    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute User userUpdate,
                                @RequestParam(required = false) MultipartFile profilePicture,
                                Principal principal) {
        userService.updateProfile(principal.getName(), userUpdate, profilePicture);
        return "redirect:/profil";
    }

    @GetMapping("/challenges")
    public String showChallenges(Model model, Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        model.addAttribute("activeChallenges",
                challengeService.getUserActiveChallenges(user.getId()));
        return "profile-challenges";
    }

    @PostMapping("/challenges/{challengeId}/complete")
    public String completeChallenge(@PathVariable Long challengeId, Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        challengeService.completeChallenge(challengeId, user.getId());
        return "redirect:/profile/challenges";
    }

    @GetMapping("/quizzes")
    public String showQuizResults(Model model, Principal principal) {
        User user = userService.getCurrentUser(principal.getName());
        model.addAttribute("quizResults",
                quizService.getUserQuizResults(user.getId()));
        return "profile-quizzes";
    }

    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error";
    }
}