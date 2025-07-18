package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.UserChallenge;
import com.greenlink.greenlink.repository.QuizResultRepository;
import com.greenlink.greenlink.repository.ProductRepository;
import com.greenlink.greenlink.repository.UserChallengeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChallengeTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(ChallengeTrackingService.class);

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private ProductRepository productRepository;


    @Autowired
    private UserChallengeRepository userChallengeRepository;

    @Autowired
    private PointsService pointsService;

    @Transactional
    public void trackUserAction(Long userId, String actionType, Object actionData) {
        logger.info("Tracking user action: userId={}, actionType={}, actionData={}", userId, actionType, actionData);
        
        List<Challenge> allChallenges = challengeService.getAllChallenges();
        logger.info("Found {} total challenges", allChallenges.size());
        
        for (Challenge challenge : allChallenges) {
            logger.debug("Checking challenge: {}", challenge.getTitle());
            if (shouldUpdateChallenge(challenge, actionType, actionData)) {
                logger.info("Updating challenge: {}", challenge.getTitle());
                updateChallengeProgress(userId, challenge);
            }
        }
    }

    @Transactional
    public void checkAndActivateChallengesForUser(Long userId) {
        logger.info("Checking and activating challenges for user: {}", userId);
        
        List<Challenge> allChallenges = challengeService.getAllChallenges();
        logger.info("Found {} total challenges", allChallenges.size());
        
        for (Challenge challenge : allChallenges) {
            logger.debug("Checking challenge: {}", challenge.getTitle());
            if (shouldActivateChallenge(challenge, userId)) {
                logger.info("Activating challenge: {}", challenge.getTitle());
                updateChallengeProgress(userId, challenge);
            }
        }
    }

    private boolean shouldActivateChallenge(Challenge challenge, Long userId) {
        String title = challenge.getTitle().toLowerCase();
        logger.debug("Checking if challenge '{}' should be activated for user: {}", challenge.getTitle(), userId);
        
        // For marketplace challenges, activate if user has any items
        if (title.contains("marketplace explorer") || title.contains("green advocate") || 
            title.contains("product curator") || title.contains("list")) {
            int listedItems = getListedItemsCount(userId);
            return listedItems >= 1;
        }
        
        // For lesson challenges, activate if user has any completed lessons
        if (title.contains("first steps") || title.contains("knowledge seeker") || 
            title.contains("education master") || title.contains("community leader") ||
            (title.contains("complete") && title.contains("lesson"))) {
            int completedLessons = getCompletedLessonsCount(userId);
            return completedLessons >= 1;
        }
        
        // For other challenges, they will be activated when the specific action occurs
        return false;
    }

    private boolean shouldUpdateChallenge(Challenge challenge, String actionType, Object actionData) {
        String title = challenge.getTitle().toLowerCase();
        logger.debug("Checking if challenge '{}' should be updated for action: {}", challenge.getTitle(), actionType);
        
        switch (actionType) {
            case "PROFILE_PHOTO_UPLOADED":
                return title.contains("profile perfectionist") || 
                       (title.contains("upload") && title.contains("photo"));
                
            case "LESSON_COMPLETED":
                if (title.contains("first steps") || title.contains("knowledge seeker") || 
                    title.contains("education master") || title.contains("community leader") ||
                    (title.contains("complete") && title.contains("lesson"))) {
                    int completedLessons = getCompletedLessonsCount((Long) actionData);
                    
                    if (title.contains("first steps") || title.contains("1 lesson") || title.contains("complete 1 lesson")) {
                        return completedLessons >= 1;
                    } else if (title.contains("knowledge seeker") || title.contains("3 lessons") || title.contains("complete 3 lessons")) {
                        return completedLessons >= 1; // Activate when user has any lessons, progress will be calculated
                    } else if (title.contains("education master") || title.contains("6 lessons") || title.contains("complete 6 lessons")) {
                        return completedLessons >= 1; // Activate when user has any lessons, progress will be calculated
                    } else if (title.contains("community leader") || title.contains("10 lessons")) {
                        return completedLessons >= 1; // Activate when user has any lessons, progress will be calculated
                    }
                }
                return false;
                
            case "MARKETPLACE_ITEM_LISTED":
                if (title.contains("marketplace explorer") || title.contains("green advocate") || 
                    title.contains("product curator") || title.contains("list")) {
                    // Extract userId from ProductDto or use the actionData directly if it's already a Long
                    Long userId;
                    if (actionData instanceof com.greenlink.greenlink.dto.ProductDto) {
                        com.greenlink.greenlink.dto.ProductDto productDto = (com.greenlink.greenlink.dto.ProductDto) actionData;
                        userId = productDto.getSellerId();
                    } else {
                        userId = (Long) actionData;
                    }
                    
                    int listedItems = getListedItemsCount(userId);
                    
                    // For "first item" challenges, only activate when user has exactly 1 item
                    if (title.contains("marketplace explorer") || title.contains("first item") || title.contains("list first item")) {
                        return listedItems >= 1;
                    } 
                    // For counting challenges, activate when user has ANY items so progress can be tracked
                    else if (title.contains("green advocate") || title.contains("10 items") || title.contains("list 10 items")) {
                        return listedItems >= 1; // Activate when user has any items, progress will be calculated
                    } else if (title.contains("product curator") || title.contains("20 items") || title.contains("list 20 items")) {
                        return listedItems >= 1; // Activate when user has any items, progress will be calculated
                    } else if (title.contains("3 items") || title.contains("list 3 items")) {
                        return listedItems >= 1; // Activate when user has any items, progress will be calculated
                    } else if (title.contains("5 items") || title.contains("list 5 items")) {
                        return listedItems >= 1; // Activate when user has any items, progress will be calculated
                    } else if (title.contains("15 items") || title.contains("list 15 items")) {
                        return listedItems >= 1; // Activate when user has any items, progress will be calculated
                    }
                }
                return false;
                
            case "MARKETPLACE_ITEM_PURCHASED":
                if (title.contains("marketplace veteran") || title.contains("buy")) {
                    if (title.contains("first item") || title.contains("buy first item")) {
                        return true;
                    } else if (title.contains("marketplace veteran") || title.contains("15 items") || title.contains("buy 15 items")) {
                        return getPurchasedItemsCount((Long) actionData) >= 15;
                    } else if (title.contains("3 items") || title.contains("buy 3 items")) {
                        return getPurchasedItemsCount((Long) actionData) >= 3;
                    } else if (title.contains("5 items") || title.contains("buy 5 items")) {
                        return getPurchasedItemsCount((Long) actionData) >= 5;
                    } else if (title.contains("10 items") || title.contains("buy 10 items")) {
                        return getPurchasedItemsCount((Long) actionData) >= 10;
                    }
                }
                return false;
                
            case "CARBON_CALCULATOR_USED":
                return title.contains("carbon conscious") || title.contains("carbon expert") ||
                       title.contains("carbon calculator") || title.contains("use carbon calculator");
                
            case "RECYCLING_MAP_EXPLORED":
                return title.contains("recycling pioneer") || 
                       title.contains("recycling map") || title.contains("explore the recycling map");
                
            case "MARKETPLACE_OFFER_MADE":
                return title.contains("deal maker") || title.contains("bargain hunter") || title.contains("deal master") ||
                       (title.contains("offer") && title.contains("marketplace")) || title.contains("make an offer");
                
            case "MESSAGE_SENT":
                return title.contains("social butterfly") || 
                       (title.contains("send") && title.contains("message")) || title.contains("send your first message");
                
            case "ITEM_FAVORITED":
                return title.contains("add") && title.contains("favorites") || title.contains("add an item to favorites");
                
            case "FRIEND_ADDED":
                if (title.contains("friend")) {
                    if (title.contains("first friend")) {
                        return true;
                    } else if (title.contains("three friends")) {
                        return getFriendsCount((Long) actionData) >= 3;
                    } else if (title.contains("five friends")) {
                        return getFriendsCount((Long) actionData) >= 5;
                    } else if (title.contains("ten friends")) {
                        return getFriendsCount((Long) actionData) >= 10;
                    } else if (title.contains("fifteen friends")) {
                        return getFriendsCount((Long) actionData) >= 15;
                    }
                }
                return false;
                
            default:
                return false;
        }
    }

    private void updateChallengeProgress(Long userId, Challenge challenge) {
        logger.info("Updating progress for challenge: {} (userId: {})", challenge.getTitle(), userId);
        
        // Get or create user challenge
        Optional<UserChallenge> existingUserChallenge = userChallengeRepository.findByUserIdAndChallengeId(userId, challenge.getId());

        UserChallenge userChallenge;
        
        if (existingUserChallenge.isPresent()) {
            userChallenge = existingUserChallenge.get();
            logger.info("Found existing user challenge with progress: {}%", userChallenge.getProgressPercentage());
        } else {
            // Create new user challenge automatically
            logger.info("Creating new user challenge automatically");
            userChallenge = challengeService.startChallenge(userId, challenge.getId());
            
            // Send notification for new challenge activation
            sendChallengeActivated(userId, challenge);
        }

        // Calculate progress based on challenge type
        int newProgress = calculateProgress(challenge, userId);
        logger.info("Calculated new progress: {}% (current: {}%)", newProgress, userChallenge.getProgressPercentage());
        
        if (newProgress > userChallenge.getProgressPercentage()) {
            userChallenge.setProgressPercentage(newProgress);
            
            // Set current value based on the action type and challenge
            int currentValue = getCurrentValueForChallenge(challenge, userId);
            userChallenge.setCurrentValue(currentValue);
            
            userChallengeRepository.save(userChallenge);
            logger.info("Updated challenge progress to: {}% with current value: {}", newProgress, currentValue);
            
            // Send real-time update via WebSocket
            sendProgressUpdate(userId, challenge.getId(), newProgress, challenge);
            
            // Check if challenge is completed
            if (newProgress >= 100) {
                logger.info("Challenge completed: {}", challenge.getTitle());
                sendChallengeCompleted(userId, challenge);
            }
        } else {
            logger.info("No progress update needed (new: {}%, current: {}%)", newProgress, userChallenge.getProgressPercentage());
        }
    }

    private int calculateProgress(Challenge challenge, Long userId) {
        String title = challenge.getTitle().toLowerCase();
        logger.debug("Calculating progress for challenge: '{}' (userId: {})", challenge.getTitle(), userId);
        
        // For single action challenges (like profile photo upload), reaching the condition means 100% progress
        if (title.contains("profile perfectionist") || (title.contains("upload") && title.contains("photo"))) {
            logger.debug("Challenge is profile photo upload - returning 100%");
            return 100;
        }
        
        if (title.contains("carbon conscious") || title.contains("carbon expert") || 
            title.contains("carbon calculator") || title.contains("use carbon calculator")) {
            logger.debug("Challenge is carbon calculator - returning 100%");
            return 100;
        }
        
        if (title.contains("recycling pioneer") || 
            title.contains("recycling map") || title.contains("explore the recycling map")) {
            logger.debug("Challenge is recycling map - returning 100%");
            return 100;
        }
        
        if (title.contains("deal maker") || title.contains("bargain hunter") || title.contains("deal master") ||
            (title.contains("offer") && title.contains("marketplace")) || title.contains("make an offer")) {
            logger.debug("Challenge is marketplace offer - returning 100%");
            return 100;
        }
        
        if (title.contains("social butterfly") || 
            (title.contains("send") && title.contains("message")) || title.contains("send your first message")) {
            logger.debug("Challenge is send message - returning 100%");
            return 100;
        }
        
        if (title.contains("add") && title.contains("favorites") || title.contains("add an item to favorites")) {
            logger.debug("Challenge is add to favorites - returning 100%");
            return 100;
        }
        
        // For counting challenges, calculate percentage based on actual counts
        if (title.contains("first steps") || title.contains("knowledge seeker") || 
            title.contains("education master") || title.contains("community leader") ||
            (title.contains("complete") && title.contains("lesson"))) {
            int completedLessons = getCompletedLessonsCount(userId);
            logger.debug("Completed lessons count: {}", completedLessons);
            
            if (title.contains("first steps") || title.contains("1 lesson") || title.contains("complete 1 lesson")) {
                int progress = completedLessons >= 1 ? 100 : 0;
                logger.debug("First lesson challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("knowledge seeker") || title.contains("3 lessons") || title.contains("complete 3 lessons")) {
                int progress = Math.min(100, (completedLessons * 100) / 3);
                logger.debug("3 lessons challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("education master") || title.contains("6 lessons") || title.contains("complete 6 lessons")) {
                int progress = Math.min(100, (completedLessons * 100) / 6);
                logger.debug("6 lessons challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("community leader") || title.contains("10 lessons")) {
                int progress = Math.min(100, (completedLessons * 100) / 10);
                logger.debug("10 lessons challenge - progress: {}%", progress);
                return progress;
            }
        }
        
        if (title.contains("marketplace explorer") || title.contains("green advocate") || 
            title.contains("product curator") || title.contains("list")) {
            int listedItems = getListedItemsCount(userId);
            logger.debug("Listed items count: {}", listedItems);
            
            if (title.contains("marketplace explorer") || title.contains("first item") || title.contains("list first item")) {
                int progress = listedItems >= 1 ? 100 : 0;
                logger.debug("First item challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("green advocate") || title.contains("10 items") || title.contains("list 10 items")) {
                int progress = Math.min(100, (listedItems * 100) / 10);
                logger.debug("10 items challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("product curator") || title.contains("20 items") || title.contains("list 20 items")) {
                int progress = Math.min(100, (listedItems * 100) / 20);
                logger.debug("20 items challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("3 items") || title.contains("list 3 items")) {
                int progress = Math.min(100, (listedItems * 100) / 3);
                logger.debug("3 items challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("5 items") || title.contains("list 5 items")) {
                int progress = Math.min(100, (listedItems * 100) / 5);
                logger.debug("5 items challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("15 items") || title.contains("list 15 items")) {
                int progress = Math.min(100, (listedItems * 100) / 15);
                logger.info("=== DEBUG 15 ITEMS CHALLENGE ===");
                logger.info("User ID: {}", userId);
                logger.info("Listed items count: {}", listedItems);
                logger.info("Calculated progress: {}%", progress);
                logger.info("================================");
                return progress;
            }
        }
        
        if (title.contains("marketplace veteran") || title.contains("buy")) {
            int purchasedItems = getPurchasedItemsCount(userId);
            logger.debug("Purchased items count: {}", purchasedItems);
            
            if (title.contains("first item") || title.contains("buy first item")) {
                int progress = purchasedItems >= 1 ? 100 : 0;
                logger.debug("Buy first item challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("marketplace veteran") || title.contains("15 items") || title.contains("buy 15 items")) {
                int progress = Math.min(100, (purchasedItems * 100) / 15);
                logger.debug("Buy 15 items challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("3 items") || title.contains("buy 3 items")) {
                int progress = Math.min(100, (purchasedItems * 100) / 3);
                logger.debug("Buy 3 items challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("5 items") || title.contains("buy 5 items")) {
                int progress = Math.min(100, (purchasedItems * 100) / 5);
                logger.debug("Buy 5 items challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("10 items") || title.contains("buy 10 items")) {
                int progress = Math.min(100, (purchasedItems * 100) / 10);
                logger.debug("Buy 10 items challenge - progress: {}%", progress);
                return progress;
            }
        }
        
        if (title.contains("friend")) {
            int friendsCount = getFriendsCount(userId);
            logger.debug("Friends count: {}", friendsCount);
            
            if (title.contains("first friend")) {
                int progress = friendsCount >= 1 ? 100 : 0;
                logger.debug("First friend challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("three friends")) {
                int progress = Math.min(100, (friendsCount * 100) / 3);
                logger.debug("Three friends challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("five friends")) {
                int progress = Math.min(100, (friendsCount * 100) / 5);
                logger.debug("Five friends challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("ten friends")) {
                int progress = Math.min(100, (friendsCount * 100) / 10);
                logger.debug("Ten friends challenge - progress: {}%", progress);
                return progress;
            } else if (title.contains("fifteen friends")) {
                int progress = Math.min(100, (friendsCount * 100) / 15);
                logger.debug("Fifteen friends challenge - progress: {}%", progress);
                return progress;
            }
        }
        
        logger.debug("No matching challenge type found - returning 0%");
        return 0;
    }
    
    private int getCurrentValueForChallenge(Challenge challenge, Long userId) {
        String title = challenge.getTitle().toLowerCase();
        
        // For single action challenges, return 1 when completed
        if (title.contains("profile perfectionist") || (title.contains("upload") && title.contains("photo"))) {
            return 1;
        }
        
        if (title.contains("carbon conscious") || title.contains("carbon expert") || 
            title.contains("carbon calculator") || title.contains("use carbon calculator")) {
            return 1;
        }
        
        if (title.contains("recycling pioneer") || 
            title.contains("recycling map") || title.contains("explore the recycling map")) {
            return 1;
        }
        
        if (title.contains("deal maker") || title.contains("bargain hunter") || title.contains("deal master") ||
            (title.contains("offer") && title.contains("marketplace")) || title.contains("make an offer")) {
            return 1;
        }
        
        if (title.contains("social butterfly") || 
            (title.contains("send") && title.contains("message")) || title.contains("send your first message")) {
            return 1;
        }
        
        if (title.contains("add") && title.contains("favorites") || title.contains("add an item to favorites")) {
            return 1;
        }
        
        // For counting challenges, return the actual count
        if (title.contains("first steps") || title.contains("knowledge seeker") || 
            title.contains("education master") || title.contains("community leader") ||
            title.contains("complete") || title.contains("lesson")) {
            return getCompletedLessonsCount(userId);
        }
        
        if (title.contains("marketplace explorer") || title.contains("green advocate") || 
            title.contains("product curator") || title.contains("list")) {
            return getListedItemsCount(userId);
        }
        
        if (title.contains("marketplace veteran") || title.contains("buy")) {
            return getPurchasedItemsCount(userId);
        }
        
        if (title.contains("friend")) {
            return getFriendsCount(userId);
        }
        
        // Default to 0
        return 0;
    }

    private void sendProgressUpdate(Long userId, Long challengeId, int progress, Challenge challenge) {
        ChallengeProgressUpdate update = new ChallengeProgressUpdate(challengeId, progress, challenge.getTitle(), challenge.getTargetValue());
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/challenge-progress", update);
    }

        private void sendChallengeActivated(Long userId, Challenge challenge) {
        ChallengeActivatedEvent event = new ChallengeActivatedEvent(
            challenge.getId(),
            challenge.getTitle(),
            challenge.getDescription(),
            challenge.getPoints(),
            challenge.getBadge()
        );
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/challenge-activated", event);
    }
    
    private void sendChallengeCompleted(Long userId, Challenge challenge) {
        // Award points for challenge completion
        try {
            pointsService.awardChallengePoints(userId, challenge.getId(), challenge.getTitle(), challenge.getPoints());
        } catch (Exception e) {
            logger.error("Error awarding points for challenge completion: {}", e.getMessage(), e);
        }
        
        ChallengeCompletedEvent event = new ChallengeCompletedEvent(
            challenge.getId(),
            challenge.getTitle(),
            challenge.getPoints(),
            challenge.getBadge()
        );
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/challenge-completed", event);
    }

    // Helper methods for counting - now properly implemented
    private int getCompletedLessonsCount(Long userId) {
        int count = (int) quizResultRepository.countByUserId(userId);
        logger.debug("Completed lessons count for user {}: {}", userId, count);
        return count;
    }

    private int getListedItemsCount(Long userId) {
        int count = (int) productRepository.countBySellerId(userId);
        logger.debug("Listed items count for user {}: {}", userId, count);
        return count;
    }

    private int getPurchasedItemsCount(Long userId) {
        // For now, return 0 as we don't have a purchase system implemented
        // This would need to be implemented when you add a purchase/order system
        logger.debug("Purchased items count for user {}: 0 (not implemented)", userId);
        return 0;
    }

    private int getFriendsCount(Long userId) {
        // For now, return 0 as we don't have a friends system implemented
        // This would need to be implemented when you add a friends/following system
        logger.debug("Friends count for user {}: 0 (not implemented)", userId);
        return 0;
    }

    // DTO classes for WebSocket messages
    public static class ChallengeProgressUpdate {
        private Long challengeId;
        private int progress;
        private String title;
        private int targetValue;

        public ChallengeProgressUpdate(Long challengeId, int progress, String title, int targetValue) {
            this.challengeId = challengeId;
            this.progress = progress;
            this.title = title;
            this.targetValue = targetValue;
        }

        // Getters and setters
        public Long getChallengeId() { return challengeId; }
        public void setChallengeId(Long challengeId) { this.challengeId = challengeId; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public int getTargetValue() { return targetValue; }
        public void setTargetValue(int targetValue) { this.targetValue = targetValue; }
    }

    public static class ChallengeActivatedEvent {
        private Long challengeId;
        private String title;
        private String description;
        private int points;
        private String badge;

        public ChallengeActivatedEvent(Long challengeId, String title, String description, int points, String badge) {
            this.challengeId = challengeId;
            this.title = title;
            this.description = description;
            this.points = points;
            this.badge = badge;
        }

        // Getters and setters
        public Long getChallengeId() { return challengeId; }
        public void setChallengeId(Long challengeId) { this.challengeId = challengeId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getPoints() { return points; }
        public void setPoints(int points) { this.points = points; }
        public String getBadge() { return badge; }
        public void setBadge(String badge) { this.badge = badge; }
    }

    public static class ChallengeCompletedEvent {
        private Long challengeId;
        private String title;
        private int points;
        private String badge;

        public ChallengeCompletedEvent(Long challengeId, String title, int points, String badge) {
            this.challengeId = challengeId;
            this.title = title;
            this.points = points;
            this.badge = badge;
        }

        // Getters and setters
        public Long getChallengeId() { return challengeId; }
        public void setChallengeId(Long challengeId) { this.challengeId = challengeId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public int getPoints() { return points; }
        public void setPoints(int points) { this.points = points; }
        public String getBadge() { return badge; }
        public void setBadge(String badge) { this.badge = badge; }
    }
} 