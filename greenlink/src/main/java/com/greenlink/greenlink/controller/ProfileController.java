package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.model.UserChallenge;
import com.greenlink.greenlink.model.QuizResult;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ChallengeService;
import com.greenlink.greenlink.service.QuizService;
import com.greenlink.greenlink.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    private ProductService productService;

    @GetMapping
    public String showProfile(Model model, Principal principal) {
        try {
            User user = userService.getCurrentUser(principal.getName());

            // Add user data to model
            model.addAttribute("user", user);
            model.addAttribute("totalPoints", user.getPoints());

            // Get quiz results
            List<QuizResult> quizResults = quizService.getUserQuizResults(user.getId());
            model.addAttribute("quizResults", quizResults);

            // Get challenges
            List<UserChallenge> activeChallenges = challengeService.getUserActiveChallenges(user.getId());
            List<UserChallenge> completedChallenges = challengeService.getUserCompletedChallenges(user.getId());
            model.addAttribute("activeChallenges", activeChallenges);
            model.addAttribute("completedChallenges", completedChallenges);

            return "profile";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load profile: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/edit")
    public String showEditProfile(Model model, Principal principal) {
        try {
            User user = userService.getCurrentUser(principal.getName());
            model.addAttribute("user", user);
            return "profile/edit";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load profile for editing: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute User userUpdate,
                              @RequestParam(required = false) MultipartFile profilePicture,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.updateProfile(principal.getName(), userUpdate, profilePicture);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/challenges")
    public String showChallenges(Model model, Principal principal) {
        try {
            User user = userService.getCurrentUser(principal.getName());
            List<UserChallenge> activeChallenges = challengeService.getUserActiveChallenges(user.getId());
            List<UserChallenge> completedChallenges = challengeService.getUserCompletedChallenges(user.getId());
            model.addAttribute("activeChallenges", activeChallenges);
            model.addAttribute("completedChallenges", completedChallenges);
            return "profile/challenges";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load challenges: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/challenges/{challengeId}/complete")
    public String completeChallenge(@PathVariable Long challengeId,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            redirectAttributes.addFlashAttribute("success", "Challenge completed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to complete challenge: " + e.getMessage());
        }
        return "redirect:/profile/challenges";
    }

    @GetMapping("/quizzes")
    public String showQuizResults(Model model, Principal principal) {
        try {
            User user = userService.getCurrentUser(principal.getName());
            List<QuizResult> quizResults = quizService.getUserQuizResults(user.getId());
            model.addAttribute("quizResults", quizResults);
            return "profile/quizzes";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load quiz results: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/public/{userId}")
    public String viewPublicProfile(@PathVariable Long userId, Model model) {
        try {
            // Get the user by ID
            User user = userService.getUserById(userId);
            if (user == null) {
                return "redirect:/marketplace";
            }
            
            // Get user's products
            List<ProductDto> userProducts = productService.getProductsBySeller(userId);
            
            model.addAttribute("profileUser", user);
            model.addAttribute("products", userProducts);
            
            return "profile/public-profile";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/marketplace";
        }
    }
}