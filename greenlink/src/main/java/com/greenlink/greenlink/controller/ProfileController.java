package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ChallengeTrackingService;
import com.greenlink.greenlink.service.ProductService;
import com.greenlink.greenlink.repository.QuizResultRepository;
import com.greenlink.greenlink.model.QuizResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.greenlink.greenlink.model.UserChallenge;
import com.greenlink.greenlink.service.FriendService;
import com.greenlink.greenlink.repository.PointEventRepository;
import com.greenlink.greenlink.model.PointEvent;
import com.greenlink.greenlink.service.ChallengeService;
import com.greenlink.greenlink.service.DirectMessageService;

/**
 * Controller for handling user profile-related operations.
 * Manages both personal profiles and public profile views.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final UserService userService;
    private final ChallengeTrackingService challengeTrackingService;
    private final ProductService productService;
    private final QuizResultRepository quizResultRepository;
    private final FriendService friendService;
    private final PointEventRepository pointEventRepository;
    private final ChallengeService challengeService;
    private final DirectMessageService directMessageService;

    @Autowired
    public ProfileController(UserService userService,
                           ChallengeTrackingService challengeTrackingService,
                           ProductService productService,
                           QuizResultRepository quizResultRepository,
                           FriendService friendService,
                           PointEventRepository pointEventRepository,
                           ChallengeService challengeService,
                           DirectMessageService directMessageService) {
        this.userService = userService;
        this.challengeTrackingService = challengeTrackingService;
        this.productService = productService;
        this.quizResultRepository = quizResultRepository;
        this.friendService = friendService;
        this.pointEventRepository = pointEventRepository;
        this.challengeService = challengeService;
        this.directMessageService = directMessageService;
    }

    /**
     * Display the current user's personal profile page.
     * Shows user's own data and quick actions.
     */
    @GetMapping
    public String showPersonalProfile(Model model, Principal principal) {
        try {
            if (principal == null) {
                logger.warn("Unauthenticated user attempted to access personal profile");
                return "redirect:/login";
            }

            User currentUser = userService.getCurrentUser(principal.getName());
            if (currentUser == null) {
                logger.error("Current user not found for principal: {}", principal.getName());
                model.addAttribute("error", "User session expired. Please login again.");
                return "error";
            }

            // Load personal profile data
            loadPersonalProfileData(model, currentUser);
            
            // Mark as personal profile
            model.addAttribute("isPersonalProfile", true);
            model.addAttribute("currentUser", currentUser);
            
            logger.info("Personal profile loaded successfully for user: {} - Returning template: profile/tabbed_profile", currentUser.getUsername());
            return "profile/tabbed_profile";

        } catch (Exception e) {
            logger.error("Error loading personal profile: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load profile. Please try again.");
            return "error";
        }
    }

    /**
     * Display the edit profile page for the current user.
     */
    @GetMapping("/edit")
    public String showEditProfile(Model model, Principal principal) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            User currentUser = userService.getCurrentUser(principal.getName());
            if (currentUser == null) {
                model.addAttribute("error", "User session expired. Please login again.");
                return "error";
            }

            model.addAttribute("user", currentUser);
            return "profile/edit";

        } catch (Exception e) {
            logger.error("Error loading edit profile page: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load edit profile page.");
            return "error";
        }
    }

    /**
     * Handle profile update form submission.
     */
    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute User userUpdate,
                              @RequestParam(required = false) MultipartFile profilePicture,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            User updatedUser = userService.updateProfile(principal.getName(), userUpdate, profilePicture);
            
            // Track profile photo upload for challenges if applicable
            if (profilePicture != null && !profilePicture.isEmpty()) {
                challengeTrackingService.trackUserAction(updatedUser.getId(), "PROFILE_PHOTO_UPLOADED", null);
                logger.info("Profile photo uploaded for user: {}", updatedUser.getUsername());
            }
            
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            logger.info("Profile updated successfully for user: {}", updatedUser.getUsername());

        } catch (Exception e) {
            logger.error("Error updating profile: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    /**
     * Display a public profile page for any user by ID.
     * Shows user's public information and products.
     */
    @GetMapping("/user/{userId}")
    public String viewPublicProfile(@PathVariable Long userId, Model model, Principal principal) {
        try {
            // Validate input
            if (userId == null || userId <= 0) {
                logger.warn("Invalid user ID provided: {}", userId);
                model.addAttribute("error", "Invalid user ID provided.");
                return "error";
            }

            // Get the target user
            User targetUser = userService.getUserById(userId);
            if (targetUser == null) {
                logger.warn("User with ID {} not found", userId);
                model.addAttribute("error", "User not found.");
                return "error";
            }

            // Check if user is viewing their own profile
            User currentUser = null;
            boolean isOwnProfile = false;
            if (principal != null) {
                try {
                    currentUser = userService.getCurrentUser(principal.getName());
                    isOwnProfile = currentUser != null && currentUser.getId().equals(targetUser.getId());
                } catch (Exception e) {
                    logger.warn("Could not get current user: {}", e.getMessage());
                    // Continue without current user
                }
            }

            // Set the profile user first
            model.addAttribute("profileUser", targetUser);
            
            // Load public profile data
            loadPublicProfileData(model, targetUser, principal);
            
            // Mark as public profile
            model.addAttribute("isPersonalProfile", false);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isOwnProfile", isOwnProfile);
            
            // Check if current user can send DM to target user (friends only)
            if (currentUser != null && !isOwnProfile) {
                boolean canSendMessage = directMessageService.canSendMessage(currentUser, targetUser);
                model.addAttribute("canSendMessage", canSendMessage);
            } else {
                model.addAttribute("canSendMessage", false);
            }
            
            logger.info("Public profile loaded successfully for user ID: {} ({}) - Returning template: profile/tabbed_profile", userId, targetUser.getUsername());
            return "profile/tabbed_profile";

        } catch (Exception e) {
            logger.error("Error loading public profile for user ID {}: {}", userId, e.getMessage(), e);
            model.addAttribute("error", "Failed to load profile. Please try again later.");
            return "error";
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Load data for personal profile page.
     */
    private void loadPersonalProfileData(Model model, User currentUser) {
        // Basic user data
        model.addAttribute("profileUser", currentUser);

        // User's products
        try {
            List<ProductDto> userProducts = productService.getProductsBySeller(currentUser.getId());
            model.addAttribute("products", userProducts != null ? userProducts : List.of());
        } catch (Exception e) {
            logger.warn("Failed to load products for user {}: {}", currentUser.getId(), e.getMessage());
            model.addAttribute("products", List.of());
        }
        
        // User's completed challenges and badges
        try {
            List<UserChallenge> completedChallenges = challengeService.getUserCompletedChallenges(currentUser.getId());
            model.addAttribute("completedChallenges", completedChallenges != null ? completedChallenges : List.of());
            
            // Extract unique badges from completed challenges
            List<String> earnedBadges = (completedChallenges != null ? completedChallenges.stream()
                .map(uc -> uc.getChallenge().getBadge())
                .filter(badge -> badge != null && !badge.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList()) : List.of());
            model.addAttribute("earnedBadges", earnedBadges);
            
        } catch (Exception e) {
            logger.warn("Failed to load challenges for user {}: {}", currentUser.getId(), e.getMessage());
            model.addAttribute("completedChallenges", List.of());
            model.addAttribute("earnedBadges", List.of());
        }
        
        // User's friends
        try {
            List<User> friends = friendService.getFriendsList(currentUser);
            model.addAttribute("friends", friends != null ? friends : List.of());
        } catch (Exception e) {
            logger.warn("Failed to load friends for user {}: {}", currentUser.getId(), e.getMessage());
            model.addAttribute("friends", List.of());
        }
        
        // Recent activity (last 5 point events)
        try {
            List<PointEvent> recentActivity = pointEventRepository.findTop5ByUserIdOrderByCreatedAtDesc(currentUser.getId());
            model.addAttribute("recentActivity", recentActivity != null ? recentActivity : List.of());
        } catch (Exception e) {
            logger.warn("Failed to load recent activity for user {}: {}", currentUser.getId(), e.getMessage());
            model.addAttribute("recentActivity", List.of());
        }
        
        // Reviews (placeholder for now - would need a review system)
        model.addAttribute("reviews", List.of());
        model.addAttribute("averageRating", 0.0);
        model.addAttribute("totalReviews", 0);
        
        // User's rank
        try {
            int userRank = userService.getUserRank(currentUser.getId());
            model.addAttribute("userRank", userRank);
        } catch (Exception e) {
            logger.warn("Failed to load rank for user {}: {}", currentUser.getId(), e.getMessage());
            model.addAttribute("userRank", 0);
        }
        
        // Load completed lessons for the quizzes section
        loadCompletedLessons(model, currentUser);
    }

    /**
     * Load data for public profile page.
     */
    private void loadPublicProfileData(Model model, User targetUser, Principal principal) {
        // Note: profileUser is already set in the controller method

        // User's products
        try {
            List<ProductDto> userProducts = productService.getProductsBySeller(targetUser.getId());
            model.addAttribute("products", userProducts != null ? userProducts : List.of());
        } catch (Exception e) {
            logger.warn("Failed to load products for user {}: {}", targetUser.getId(), e.getMessage());
            model.addAttribute("products", List.of());
        }
        
        // User's completed challenges and badges
        try {
            List<UserChallenge> completedChallenges = challengeService.getUserCompletedChallenges(targetUser.getId());
            model.addAttribute("completedChallenges", completedChallenges != null ? completedChallenges : List.of());
            
            // Extract unique badges from completed challenges
            List<String> earnedBadges = (completedChallenges != null ? completedChallenges.stream()
                .map(uc -> uc.getChallenge().getBadge())
                .filter(badge -> badge != null && !badge.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList()) : List.of());
            model.addAttribute("earnedBadges", earnedBadges);
            
        } catch (Exception e) {
            logger.warn("Failed to load challenges for user {}: {}", targetUser.getId(), e.getMessage());
            model.addAttribute("completedChallenges", List.of());
            model.addAttribute("earnedBadges", List.of());
        }
        
        // User's friends
        try {
            List<User> friends = friendService.getFriendsList(targetUser);
            model.addAttribute("friends", friends != null ? friends : List.of());
        } catch (Exception e) {
            logger.warn("Failed to load friends for user {}: {}", targetUser.getId(), e.getMessage());
            model.addAttribute("friends", List.of());
        }
        
        // Recent activity (last 5 point events)
        try {
            List<PointEvent> recentActivity = pointEventRepository.findTop5ByUserIdOrderByCreatedAtDesc(targetUser.getId());
            model.addAttribute("recentActivity", recentActivity != null ? recentActivity : List.of());
        } catch (Exception e) {
            logger.warn("Failed to load recent activity for user {}: {}", targetUser.getId(), e.getMessage());
            model.addAttribute("recentActivity", List.of());
        }
        
        // Reviews (placeholder for now - would need a review system)
        model.addAttribute("reviews", List.of());
        model.addAttribute("averageRating", 0.0);
        model.addAttribute("totalReviews", 0);
        
        // User's rank
        try {
            int userRank = userService.getUserRank(targetUser.getId());
            model.addAttribute("userRank", userRank);
        } catch (Exception e) {
            logger.warn("Failed to load rank for user {}: {}", targetUser.getId(), e.getMessage());
            model.addAttribute("userRank", 0);
        }
        
        // Friend status for current user
        if (principal != null) {
            try {
                User currentUser = userService.getCurrentUser(principal.getName());
                boolean isFriend = friendService.areFriends(currentUser.getId(), targetUser.getId());
                boolean friendRequestSent = friendService.hasFriendRequest(currentUser.getId(), targetUser.getId());
                
                model.addAttribute("isFriend", isFriend);
                model.addAttribute("friendRequestSent", friendRequestSent);
            } catch (Exception e) {
                logger.warn("Failed to load friend status for user {}: {}", targetUser.getId(), e.getMessage());
                model.addAttribute("isFriend", false);
                model.addAttribute("friendRequestSent", false);
            }
        } else {
            model.addAttribute("isFriend", false);
            model.addAttribute("friendRequestSent", false);
        }
        
        // Load completed lessons for the quizzes section
        loadCompletedLessons(model, targetUser);
    }
    
    /**
     * Load completed lessons data for the user profile.
     */
    private void loadCompletedLessons(Model model, User user) {
        try {
            // Get all completed lessons (quiz results) for the user
            List<QuizResult> completedLessons = quizResultRepository.findByUserId(user.getId());
            
            // Create a list of lesson completion data with enhanced information
            List<LessonCompletionData> lessonCompletions = completedLessons.stream()
                .map(this::mapToLessonCompletionData)
                .collect(Collectors.toList());
            
            model.addAttribute("completedLessons", lessonCompletions);
            
            // Legacy support - keep existing quiz results for backward compatibility
            model.addAttribute("quizResults", completedLessons);
            
            logger.info("Loaded {} completed lessons for user: {}", completedLessons.size(), user.getUsername());
            
        } catch (Exception e) {
            logger.error("Failed to load completed lessons for user {}: {}", user.getId(), e.getMessage(), e);
            model.addAttribute("completedLessons", List.of());
            model.addAttribute("quizResults", List.of());
        }
    }
    
    /**
     * Map QuizResult to LessonCompletionData with enhanced information.
     */
    private LessonCompletionData mapToLessonCompletionData(QuizResult quizResult) {
        LessonCompletionData lessonData = new LessonCompletionData();
        
        // Get the quiz ID to determine which course this is
        Long quizId = quizResult.getQuiz() != null ? quizResult.getQuiz().getId() : null;
        
        if (quizId != null) {
            // Map course information based on quiz ID
            switch (quizId.intValue()) {
                case 1:
                    lessonData.setTitle("1. Ce este sustenabilitatea?");
                    lessonData.setDescription("Învață principiile fundamentale ale sustenabilității și cum să aplici aceste concepte în viața de zi cu zi.");
                    lessonData.setImageUrl("/images/education/Ecologie si Biodiversitate.png");
                    lessonData.setCourseNumber(1);
                    break;
                case 2:
                    lessonData.setTitle("2. Cum să reciclezi corect");
                    lessonData.setDescription("Descoperă regulile de bază pentru reciclare și cum să organizezi deșeurile eficient.");
                    lessonData.setImageUrl("/images/education/Procesul de reciclare.png");
                    lessonData.setCourseNumber(2);
                    break;
                case 3:
                    lessonData.setTitle("3. Reduce deșeurile cu o singură schimbare");
                    lessonData.setDescription("Învață schimbări mici care au un impact mare în reducerea deșeurilor din casa ta.");
                    lessonData.setImageUrl("/images/education/Introducere in reciclare.png");
                    lessonData.setCourseNumber(3);
                    break;
                case 4:
                    lessonData.setTitle("4. Începe un mini-compost acasă");
                    lessonData.setDescription("Transformă resturile în viață nouă");
                    lessonData.setImageUrl("/images/education/Ecosisteme si Biodiversitate.png");
                    lessonData.setCourseNumber(4);
                    break;
                case 5:
                    lessonData.setTitle("5. Plantează ceva (chiar și într-un ghiveci!)");
                    lessonData.setDescription("Conectează-te cu natura prin plantat");
                    lessonData.setImageUrl("/images/education/Energie Verde si Sustenabilitate.png");
                    lessonData.setCourseNumber(5);
                    break;
                case 6:
                    lessonData.setTitle("6. Adună gunoiul din cartierul tău");
                    lessonData.setDescription("Fă diferența în comunitatea ta");
                    lessonData.setImageUrl("/images/education/Energie Regenerabila.png");
                    lessonData.setCourseNumber(6);
                    break;
                default:
                    // Fallback to quiz data if available
                    if (quizResult.getQuiz() != null) {
                        lessonData.setTitle(quizResult.getQuiz().getTitle());
                        lessonData.setDescription(quizResult.getQuiz().getDescription());
                        lessonData.setCourseNumber(quizId.intValue());
                    } else {
                        lessonData.setTitle("Lecție completată");
                        lessonData.setDescription("Detaliile lecției nu sunt disponibile");
                        lessonData.setCourseNumber(0);
                    }
                    break;
            }
        } else {
            lessonData.setTitle("Lecție completată");
            lessonData.setDescription("Detaliile lecției nu sunt disponibile");
            lessonData.setCourseNumber(0);
        }
        
        lessonData.setPointsEarned(quizResult.getPointsEarned());
        lessonData.setCompletedAt(quizResult.getCompletedAt());
        
        return lessonData;
    }

    // ==================== DTO CLASSES ====================

    /**
     * Data transfer object for lesson completion information.
     */
    public static class LessonCompletionData {
        private String title;
        private String description;
        private int pointsEarned;
        private LocalDateTime completedAt;
        private String imageUrl;
        private int courseNumber;

        // Getters and Setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getPointsEarned() {
            return pointsEarned;
        }

        public void setPointsEarned(int pointsEarned) {
            this.pointsEarned = pointsEarned;
        }

        public LocalDateTime getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public int getCourseNumber() {
            return courseNumber;
        }

        public void setCourseNumber(int courseNumber) {
            this.courseNumber = courseNumber;
        }
    }
}