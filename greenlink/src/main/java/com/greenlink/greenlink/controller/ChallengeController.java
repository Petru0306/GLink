package com.greenlink.greenlink.controller;
import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.ChallengeService;
import com.greenlink.greenlink.service.ChallengeTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;
import com.greenlink.greenlink.model.UserChallenge;
import com.greenlink.greenlink.repository.UserChallengeRepository;
import com.greenlink.greenlink.repository.ChallengeRepository;

@Controller
@RequestMapping("/provocari")
public class ChallengeController {

    private final ChallengeService challengeService;
    
    @Autowired
    private ChallengeTrackingService challengeTrackingService;
    
    @Autowired
    private UserChallengeRepository userChallengeRepository;
    
    @Autowired
    private ChallengeRepository challengeRepository;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    public String showChallenges(Model model, @AuthenticationPrincipal User user) {
        // Add user-specific data only if user is logged in
        if (user != null) {
            // Check and update challenges based on existing user data
            checkAndUpdateUserChallenges(user.getId());
            
            // Get user's active and completed challenges
            List<UserChallenge> activeChallenges = challengeService.getTranslatedUserActiveChallenges(user.getId());
            List<UserChallenge> completedChallenges = challengeService.getTranslatedUserCompletedChallenges(user.getId());
            
            // Get all challenges and filter out the ones that are active or completed
            List<Challenge> allChallenges = challengeService.getTranslatedChallenges();
            List<Challenge> availableChallenges = filterAvailableChallenges(allChallenges, activeChallenges, completedChallenges);
            
            model.addAttribute("allChallenges", availableChallenges);
            model.addAttribute("activeChallenges", activeChallenges);
            model.addAttribute("completedChallenges", completedChallenges);
            model.addAttribute("totalPoints", challengeService.getTotalPoints(user.getId()));
            model.addAttribute("completedCount", challengeService.getCompletedChallengesCount(user.getId()));
            model.addAttribute("currentStreak", challengeService.getCurrentStreak(user.getId()));
        } else {
            // For non-logged in users, show all challenges
            model.addAttribute("allChallenges", challengeService.getTranslatedChallenges());
        }
        return "provocari";
    }


    
    // Filter challenges to show only those that are not active or completed
    private List<Challenge> filterAvailableChallenges(List<Challenge> allChallenges, 
                                                     List<UserChallenge> activeChallenges, 
                                                     List<UserChallenge> completedChallenges) {
        // Get IDs of active and completed challenges
        Set<Long> activeChallengeIds = activeChallenges.stream()
                .map(uc -> uc.getChallenge().getId())
                .collect(Collectors.toSet());
        
        Set<Long> completedChallengeIds = completedChallenges.stream()
                .map(uc -> uc.getChallenge().getId())
                .collect(Collectors.toSet());
        
        // Filter out challenges that are active or completed
        return allChallenges.stream()
                .filter(challenge -> !activeChallengeIds.contains(challenge.getId()) && 
                                   !completedChallengeIds.contains(challenge.getId()))
                .collect(Collectors.toList());
    }
    
    // Check and update challenges based on existing user data
    private void checkAndUpdateUserChallenges(Long userId) {
        try {
            // Use the new method to check and activate challenges based on existing user data
            challengeTrackingService.checkAndActivateChallengesForUser(userId);
            
        } catch (Exception e) {
            System.err.println("Error checking user challenges: " + e.getMessage());
        }
    }

    // Note: Challenges are now automatic - no manual start/progress endpoints needed
    // Challenges are activated and updated automatically when users perform actions

    @GetMapping("/type/{type}")
    public String getChallengesByType(@PathVariable Challenge.ChallengeType type, Model model) {
        // Get all translated challenges and filter by type
        List<Challenge> allTranslatedChallenges = challengeService.getTranslatedChallenges();
        List<Challenge> filteredChallenges = allTranslatedChallenges.stream()
                .filter(challenge -> challenge.getType() == type)
                .collect(Collectors.toList());
        model.addAttribute("challenges", filteredChallenges);
        return "provocari :: challengeList";
    }
    
    // Test endpoint for debugging challenge tracking
    @PostMapping("/test-tracking")
    @ResponseBody
    public String testTracking(@RequestParam String actionType, @AuthenticationPrincipal User user) {
        System.out.println("Test tracking endpoint called with actionType: " + actionType);
        System.out.println("User: " + (user != null ? user.getEmail() : "null"));
        
        if (user == null) {
            System.out.println("User not authenticated");
            return "error: User not authenticated";
        }
        
        try {
            System.out.println("Calling challenge tracking service...");
            challengeTrackingService.trackUserAction(user.getId(), actionType, null);
            System.out.println("Challenge tracking completed successfully");
            return "success: Challenge tracking triggered for action: " + actionType;
        } catch (Exception e) {
            System.out.println("Error in challenge tracking: " + e.getMessage());
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }
    
    // Simple test endpoint to verify controller accessibility
    @GetMapping("/test")
    @ResponseBody
    public String testEndpoint(@AuthenticationPrincipal User user) {
        System.out.println("Test endpoint called");
        System.out.println("User: " + (user != null ? user.getEmail() : "null"));
        return "Controller is accessible! User: " + (user != null ? user.getEmail() : "not authenticated");
    }

    // Debug endpoint to test challenge tracking and see available challenges
    @GetMapping("/debug")
    @ResponseBody
    public String debugChallenges(@AuthenticationPrincipal User user) {
        if (user == null) {
            return "User not authenticated";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("=== CHALLENGE DEBUG ===\n");
        result.append("User: ").append(user.getEmail()).append("\n");
        result.append("User ID: ").append(user.getId()).append("\n\n");
        
        // Get all challenges
        List<Challenge> allChallenges = challengeService.getAllChallenges();
        result.append("Total challenges in database: ").append(allChallenges.size()).append("\n\n");
        
        result.append("Available challenges:\n");
        for (Challenge challenge : allChallenges) {
            result.append("- ").append(challenge.getTitle())
                  .append(" (ID: ").append(challenge.getId())
                  .append(", Type: ").append(challenge.getType())
                  .append(", Target: ").append(challenge.getTargetValue())
                  .append(")\n");
        }
        
        result.append("\nUser's active challenges:\n");
        List<UserChallenge> activeChallenges = challengeService.getUserActiveChallenges(user.getId());
        for (UserChallenge uc : activeChallenges) {
            result.append("- ").append(uc.getChallenge().getTitle())
                  .append(" (Progress: ").append(uc.getProgressPercentage())
                  .append("%, Current: ").append(uc.getCurrentValue())
                  .append("/").append(uc.getChallenge().getTargetValue())
                  .append(")\n");
        }
        
        result.append("\nUser's completed challenges:\n");
        List<UserChallenge> completedChallenges = challengeService.getUserCompletedChallenges(user.getId());
        for (UserChallenge uc : completedChallenges) {
            result.append("- ").append(uc.getChallenge().getTitle())
                  .append(" (Completed: ").append(uc.getCompletedAt())
                  .append(")\n");
        }
        
        return result.toString();
    }
    
    // Endpoint to check existing data and update challenges
    @GetMapping("/check-existing-data")
    @ResponseBody
    public String checkExistingData(@AuthenticationPrincipal User user) {
        if (user == null) {
            return "User not authenticated";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("=== CHECKING EXISTING DATA ===\n");
        result.append("User: ").append(user.getEmail()).append("\n");
        result.append("User ID: ").append(user.getId()).append("\n\n");
        
        try {
            // Check and update challenges based on existing user data
            checkAndUpdateUserChallenges(user.getId());
            result.append("✓ Successfully checked and updated challenges based on existing data\n");
            
            // Get updated challenge status
            result.append("\n=== UPDATED CHALLENGE STATUS ===\n");
            
            result.append("Active challenges:\n");
            List<UserChallenge> activeChallenges = challengeService.getUserActiveChallenges(user.getId());
            for (UserChallenge uc : activeChallenges) {
                result.append("- ").append(uc.getChallenge().getTitle())
                      .append(" (Progress: ").append(uc.getProgressPercentage())
                      .append("%, Current: ").append(uc.getCurrentValue())
                      .append("/").append(uc.getChallenge().getTargetValue())
                      .append(")\n");
            }
            
            result.append("\nCompleted challenges:\n");
            List<UserChallenge> completedChallenges = challengeService.getUserCompletedChallenges(user.getId());
            for (UserChallenge uc : completedChallenges) {
                result.append("- ").append(uc.getChallenge().getTitle())
                      .append(" (Completed: ").append(uc.getCompletedAt())
                      .append(")\n");
            }
            
        } catch (Exception e) {
            result.append("✗ Error checking existing data: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }
    
    // Endpoint to manually activate challenges based on existing user data
    @PostMapping("/activate-challenges")
    @ResponseBody
    public String activateChallenges(@AuthenticationPrincipal User user) {
        if (user == null) {
            return "User not authenticated";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("=== ACTIVATING CHALLENGES ===\n");
        result.append("User: ").append(user.getEmail()).append("\n");
        result.append("User ID: ").append(user.getId()).append("\n\n");
        
        try {
            // Use the new method to activate challenges based on existing user data
            challengeTrackingService.checkAndActivateChallengesForUser(user.getId());
            result.append("✓ Successfully activated challenges based on existing data\n");
            
            // Get updated challenge status
            result.append("\n=== ACTIVATED CHALLENGES ===\n");
            
            result.append("Active challenges:\n");
            List<UserChallenge> activeChallenges = challengeService.getUserActiveChallenges(user.getId());
            for (UserChallenge uc : activeChallenges) {
                result.append("- ").append(uc.getChallenge().getTitle())
                      .append(" (Progress: ").append(uc.getProgressPercentage())
                      .append("%, Current: ").append(uc.getCurrentValue())
                      .append("/").append(uc.getChallenge().getTargetValue())
                      .append(")\n");
            }
            
        } catch (Exception e) {
            result.append("✗ Error activating challenges: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }
    
    // Debug endpoint to check for duplicate challenges
    @GetMapping("/check-duplicates")
    @ResponseBody
    public String checkDuplicates(@AuthenticationPrincipal User user) {
        if (user == null) {
            return "User not authenticated";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("=== CHECKING FOR DUPLICATE CHALLENGES ===\n");
        result.append("User: ").append(user.getEmail()).append("\n");
        result.append("User ID: ").append(user.getId()).append("\n\n");
        
        try {
            // Get all user challenges for this user
            List<UserChallenge> allUserChallenges = userChallengeRepository.findByUserId(user.getId());
            
            result.append("All user challenges (including duplicates):\n");
            for (UserChallenge uc : allUserChallenges) {
                result.append("- ").append(uc.getChallenge().getTitle())
                      .append(" (ID: ").append(uc.getId())
                      .append(", Challenge ID: ").append(uc.getChallenge().getId())
                      .append(", Status: ").append(uc.getStatus())
                      .append(", Progress: ").append(uc.getProgressPercentage())
                      .append("%)\n");
            }
            
            // Check for duplicates
            Map<Long, List<UserChallenge>> challengesByChallengeId = allUserChallenges.stream()
                    .collect(Collectors.groupingBy(uc -> uc.getChallenge().getId()));
            
            result.append("\nDuplicate analysis:\n");
            for (Map.Entry<Long, List<UserChallenge>> entry : challengesByChallengeId.entrySet()) {
                Long challengeId = entry.getKey();
                List<UserChallenge> userChallenges = entry.getValue();
                
                if (userChallenges.size() > 1) {
                    result.append("⚠️  DUPLICATE FOUND for Challenge ID ").append(challengeId)
                          .append(" (").append(userChallenges.get(0).getChallenge().getTitle())
                          .append("): ").append(userChallenges.size()).append(" records\n");
                    
                    for (UserChallenge uc : userChallenges) {
                        result.append("  - UserChallenge ID: ").append(uc.getId())
                              .append(", Status: ").append(uc.getStatus())
                              .append(", Progress: ").append(uc.getProgressPercentage())
                              .append("%\n");
                    }
                } else {
                    result.append("✓ Single record for Challenge ID ").append(challengeId)
                          .append(" (").append(userChallenges.get(0).getChallenge().getTitle())
                          .append(")\n");
                }
            }
            
        } catch (Exception e) {
            result.append("✗ Error checking duplicates: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }
    
    // Endpoint to remove duplicate challenges
    @PostMapping("/remove-duplicates")
    @ResponseBody
    public String removeDuplicates(@AuthenticationPrincipal User user) {
        if (user == null) {
            return "User not authenticated";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("=== REMOVING DUPLICATE CHALLENGES ===\n");
        result.append("User: ").append(user.getEmail()).append("\n");
        result.append("User ID: ").append(user.getId()).append("\n\n");
        
        try {
            // Get all user challenges for this user
            List<UserChallenge> allUserChallenges = userChallengeRepository.findByUserId(user.getId());
            
            // Group by challenge ID
            Map<Long, List<UserChallenge>> challengesByChallengeId = allUserChallenges.stream()
                    .collect(Collectors.groupingBy(uc -> uc.getChallenge().getId()));
            
            int removedCount = 0;
            
            for (Map.Entry<Long, List<UserChallenge>> entry : challengesByChallengeId.entrySet()) {
                List<UserChallenge> userChallenges = entry.getValue();
                
                if (userChallenges.size() > 1) {
                    // Keep the one with the highest progress, remove the rest
                    UserChallenge bestChallenge = userChallenges.stream()
                            .max(Comparator.comparingInt(UserChallenge::getProgressPercentage))
                            .orElse(userChallenges.get(0));
                    
                    for (UserChallenge uc : userChallenges) {
                        if (!uc.getId().equals(bestChallenge.getId())) {
                            userChallengeRepository.delete(uc);
                            removedCount++;
                            result.append("🗑️  Removed duplicate: ").append(uc.getChallenge().getTitle())
                                  .append(" (ID: ").append(uc.getId())
                                  .append(", Progress: ").append(uc.getProgressPercentage())
                                  .append("%)\n");
                        }
                    }
                    
                    result.append("✅ Kept: ").append(bestChallenge.getChallenge().getTitle())
                          .append(" (ID: ").append(bestChallenge.getId())
                          .append(", Progress: ").append(bestChallenge.getProgressPercentage())
                          .append("%)\n");
                }
            }
            
            result.append("\n=== SUMMARY ===\n");
            result.append("Removed ").append(removedCount).append(" duplicate records\n");
            
            // Show final state
            result.append("\nFinal active challenges:\n");
            List<UserChallenge> activeChallenges = challengeService.getUserActiveChallenges(user.getId());
            for (UserChallenge uc : activeChallenges) {
                result.append("- ").append(uc.getChallenge().getTitle())
                      .append(" (Progress: ").append(uc.getProgressPercentage())
                      .append("%, Current: ").append(uc.getCurrentValue())
                      .append("/").append(uc.getChallenge().getTargetValue())
                      .append(")\n");
            }
            
        } catch (Exception e) {
            result.append("✗ Error removing duplicates: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }
    
    // Debug endpoint to check for specific challenges
    @GetMapping("/check-specific-challenges")
    @ResponseBody
    public String checkSpecificChallenges(@AuthenticationPrincipal User user) {
        StringBuilder result = new StringBuilder();
        result.append("=== CHECKING FOR SPECIFIC CHALLENGES ===\n\n");
        
        try {
            // Check for the challenges that should be removed
            String[] challengesToCheck = {
                "Captain Compostface",
                "The Leaf Whisperer", 
                "Shutter the Litter",
                "The Sorting Sprout"
            };
            
            for (String challengeTitle : challengesToCheck) {
                List<Challenge> challenges = challengeRepository.findByTitle(challengeTitle);
                if (challenges.isEmpty()) {
                    result.append("✅ ").append(challengeTitle).append(" - NOT FOUND (removed)\n");
                } else {
                    result.append("❌ ").append(challengeTitle).append(" - STILL EXISTS:\n");
                    for (Challenge c : challenges) {
                        result.append("    - ID: ").append(c.getId())
                              .append(", Type: ").append(c.getType())
                              .append(", Points: ").append(c.getPoints())
                              .append("\n");
                    }
                }
            }
            
            result.append("\n=== ALL DEFAULT_CHALLENGES ===\n");
            List<Challenge> defaultChallenges = challengeRepository.findByType(Challenge.ChallengeType.DEFAULT_CHALLENGES);
            for (Challenge c : defaultChallenges) {
                result.append("- ").append(c.getTitle())
                      .append(" (ID: ").append(c.getId())
                      .append(", Points: ").append(c.getPoints())
                      .append(")\n");
            }
            
        } catch (Exception e) {
            result.append("✗ Error checking challenges: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }
} 