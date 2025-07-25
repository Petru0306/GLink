package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.PointEvent;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.PointEventRepository;
import com.greenlink.greenlink.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PointsServiceImpl implements PointsService {
    
    private static final Logger logger = LoggerFactory.getLogger(PointsServiceImpl.class);
    
    private final PointEventRepository pointEventRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    // Point values for different activities
    private static final int CHALLENGE_POINTS = 50;
    private static final int QUIZ_POINTS_PER_CORRECT_ANSWER = 10;
    private static final int QUIZ_REFLECTION_BONUS = 5;
    private static final int QUIZ_IMAGE_BONUS = 10;
    private static final int MARKETPLACE_SALE_POINTS_PER_100_RON = 10; // 10 points per 100 RON
    private static final int MARKETPLACE_PURCHASE_POINTS_PER_100_RON = 5; // 5 points per 100 RON
    private static final int LESSON_COMPLETION_POINTS = 25;
    private static final int CARBON_CALCULATOR_POINTS = 15;
    private static final int RECYCLING_MAP_POINTS = 10;
    private static final int PROFILE_UPDATE_POINTS = 5;
    private static final int FRIEND_ADDED_POINTS = 10;
    private static final int MESSAGE_SENT_POINTS = 2;
    private static final int OFFER_MADE_POINTS = 5;
    private static final int OFFER_ACCEPTED_POINTS = 15;
    
    @Autowired
    public PointsServiceImpl(PointEventRepository pointEventRepository, 
                           UserRepository userRepository,
                           SimpMessagingTemplate messagingTemplate) {
        this.pointEventRepository = pointEventRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }
    
    @Override
    @Transactional
    public PointEvent addPoints(Long userId, int points, String eventType, String description) {
        return addPoints(userId, points, eventType, description, null, null);
    }
    
    @Override
    @Transactional
    public PointEvent addPoints(Long userId, int points, String eventType, String description, 
                              Long relatedEntityId, String relatedEntityType) {
        logger.info("Adding {} points to user {} for event: {}", points, userId, eventType);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        int oldLevel = user.getLevel();
        int oldPoints = user.getPoints();
        
        // Update user points
        user.setPoints(oldPoints + points);
        
        // Calculate and update the new level based on total points
        int newLevel = user.calculateLevel(user.getPoints());
        user.setLevel(newLevel);
        
        boolean leveledUp = newLevel > oldLevel;
        
        // Save user
        userRepository.save(user);
        
        // Create point event
        PointEvent pointEvent = new PointEvent(user, points, eventType, description, relatedEntityId, relatedEntityType);
        pointEvent.setOldLevel(oldLevel);
        pointEvent.setNewLevel(newLevel);
        pointEvent.setLeveledUp(leveledUp);
        
        PointEvent savedEvent = pointEventRepository.save(pointEvent);
        
        logger.info("User {} now has {} points and is level {}", userId, user.getPoints(), user.getLevel());
        
        // Send real-time notification if user leveled up
        if (leveledUp) {
            sendLevelUpNotification(userId, newLevel, points);
        }
        
        // Send points earned notification
        sendPointsEarnedNotification(userId, points, eventType, description);
        
        return savedEvent;
    }
    
    @Override
    public List<PointEvent> getUserPointHistory(Long userId) {
        return pointEventRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public Page<PointEvent> getUserPointHistory(Long userId, Pageable pageable) {
        return pointEventRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    @Override
    public List<PointEvent> getUserPointHistoryByEventType(Long userId, String eventType) {
        return pointEventRepository.findByUserIdAndEventTypeOrderByCreatedAtDesc(userId, eventType);
    }
    
    @Override
    public int getTotalPointsEarned(Long userId) {
        Integer total = pointEventRepository.getTotalPointsEarned(userId);
        return total != null ? total : 0;
    }
    
    @Override
    public int getTotalPointsByEventType(Long userId, String eventType) {
        Integer total = pointEventRepository.getTotalPointsByEventType(userId, eventType);
        return total != null ? total : 0;
    }
    
    @Override
    public long getLevelUpCount(Long userId) {
        return pointEventRepository.getLevelUpCount(userId);
    }
    
    @Override
    public List<PointEvent> getRecentEvents(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return pointEventRepository.findRecentEvents(userId, since);
    }
    
    @Override
    public Map<String, Long> getEventTypeStats(Long userId) {
        List<Object[]> stats = pointEventRepository.getEventTypeStats(userId);
        Map<String, Long> result = new HashMap<>();
        
        for (Object[] stat : stats) {
            String eventType = (String) stat[0];
            Long count = (Long) stat[1];
            result.put(eventType, count);
        }
        
        return result;
    }
    
    @Override
    public List<PointEvent> getLevelUpEvents(Long userId) {
        return pointEventRepository.findLevelUpEvents(userId);
    }
    
    @Override
    @Transactional
    public PointEvent awardChallengePoints(Long userId, Long challengeId, String challengeTitle, int points) {
        String description = String.format("Completed challenge: %s", challengeTitle);
        return addPoints(userId, points, PointEvent.EventType.CHALLENGE_COMPLETED, description, challengeId, "CHALLENGE");
    }
    
    @Override
    @Transactional
    public PointEvent awardQuizPoints(Long userId, Long quizId, String quizTitle, int points) {
        String description = String.format("Completed quiz: %s", quizTitle);
        return addPoints(userId, points, PointEvent.EventType.QUIZ_COMPLETED, description, quizId, "QUIZ");
    }
    
    @Override
    @Transactional
    public PointEvent awardMarketplaceSalePoints(Long userId, Long productId, String productName, double saleAmount) {
        int points = calculateMarketplaceSalePoints(saleAmount);
        String description = String.format("Sold product: %s for %.2f RON", productName, saleAmount);
        return addPoints(userId, points, PointEvent.EventType.MARKETPLACE_SALE, description, productId, "PRODUCT");
    }
    
    @Override
    @Transactional
    public PointEvent awardMarketplacePurchasePoints(Long userId, Long productId, String productName, double purchaseAmount) {
        int points = calculateMarketplacePurchasePoints(purchaseAmount);
        String description = String.format("Purchased product: %s for %.2f RON", productName, purchaseAmount);
        return addPoints(userId, points, PointEvent.EventType.MARKETPLACE_PURCHASE, description, productId, "PRODUCT");
    }
    
    @Override
    @Transactional
    public PointEvent awardLessonPoints(Long userId, Long lessonId, String lessonTitle, int points) {
        String description = String.format("Completed lesson: %s", lessonTitle);
        return addPoints(userId, points, PointEvent.EventType.LESSON_COMPLETED, description, lessonId, "LESSON");
    }
    
    @Override
    @Transactional
    public PointEvent awardCarbonCalculatorPoints(Long userId, int points) {
        String description = "Used carbon calculator";
        return addPoints(userId, points, PointEvent.EventType.CARBON_CALCULATOR_USED, description);
    }
    
    @Override
    @Transactional
    public PointEvent awardRecyclingMapPoints(Long userId, int points) {
        String description = "Explored recycling map";
        return addPoints(userId, points, PointEvent.EventType.RECYCLING_MAP_EXPLORED, description);
    }
    
    @Override
    @Transactional
    public PointEvent awardProfileUpdatePoints(Long userId, int points) {
        String description = "Updated profile information";
        return addPoints(userId, points, PointEvent.EventType.PROFILE_UPDATED, description);
    }
    
    @Override
    @Transactional
    public PointEvent awardFriendAddedPoints(Long userId, Long friendId, String friendName, int points) {
        String description = String.format("Added friend: %s", friendName);
        return addPoints(userId, points, PointEvent.EventType.FRIEND_ADDED, description, friendId, "USER");
    }
    
    @Override
    @Transactional
    public PointEvent awardMessageSentPoints(Long userId, Long conversationId, int points) {
        String description = "Sent a message";
        return addPoints(userId, points, PointEvent.EventType.MESSAGE_SENT, description, conversationId, "CONVERSATION");
    }
    
    @Override
    @Transactional
    public PointEvent awardOfferMadePoints(Long userId, Long productId, String productName, double offerAmount, int points) {
        String description = String.format("Made offer of %.2f RON for: %s", offerAmount, productName);
        return addPoints(userId, points, PointEvent.EventType.OFFER_MADE, description, productId, "PRODUCT");
    }
    
    @Override
    @Transactional
    public PointEvent awardOfferAcceptedPoints(Long userId, Long productId, String productName, double acceptedAmount, int points) {
        String description = String.format("Accepted offer of %.2f RON for: %s", acceptedAmount, productName);
        return addPoints(userId, points, PointEvent.EventType.OFFER_ACCEPTED, description, productId, "PRODUCT");
    }
    
    // Helper methods for calculating points
    private int calculateMarketplaceSalePoints(double saleAmount) {
        return (int) Math.floor(saleAmount / 100.0) * MARKETPLACE_SALE_POINTS_PER_100_RON;
    }
    
    private int calculateMarketplacePurchasePoints(double purchaseAmount) {
        return (int) Math.floor(purchaseAmount / 100.0) * MARKETPLACE_PURCHASE_POINTS_PER_100_RON;
    }
    
    // Real-time notification methods
    private void sendLevelUpNotification(Long userId, int newLevel, int pointsEarned) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "LEVEL_UP");
        notification.put("newLevel", newLevel);
        notification.put("pointsEarned", pointsEarned);
        notification.put("message", String.format("🎉 Congratulations! You've reached Level %d!", newLevel));
        
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", notification);
        logger.info("Sent level up notification to user {}: Level {}", userId, newLevel);
    }
    
    private void sendPointsEarnedNotification(Long userId, int points, String eventType, String description) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "POINTS_EARNED");
        notification.put("points", points);
        notification.put("eventType", eventType);
        notification.put("description", description);
        notification.put("message", String.format("+%d points earned: %s", points, description));
        
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", notification);
        logger.info("Sent points earned notification to user {}: +{} points", userId, points);
    }
    
    // Public constants for point values
    public static int getChallengePoints() { return CHALLENGE_POINTS; }
    public static int getQuizPointsPerCorrectAnswer() { return QUIZ_POINTS_PER_CORRECT_ANSWER; }
    public static int getQuizReflectionBonus() { return QUIZ_REFLECTION_BONUS; }
    public static int getQuizImageBonus() { return QUIZ_IMAGE_BONUS; }
    public static int getMarketplaceSalePointsPer100Ron() { return MARKETPLACE_SALE_POINTS_PER_100_RON; }
    public static int getMarketplacePurchasePointsPer100Ron() { return MARKETPLACE_PURCHASE_POINTS_PER_100_RON; }
    public static int getLessonCompletionPoints() { return LESSON_COMPLETION_POINTS; }
    public static int getCarbonCalculatorPoints() { return CARBON_CALCULATOR_POINTS; }
    public static int getRecyclingMapPoints() { return RECYCLING_MAP_POINTS; }
    public static int getProfileUpdatePoints() { return PROFILE_UPDATE_POINTS; }
    public static int getFriendAddedPoints() { return FRIEND_ADDED_POINTS; }
    public static int getMessageSentPoints() { return MESSAGE_SENT_POINTS; }
    public static int getOfferMadePoints() { return OFFER_MADE_POINTS; }
    public static int getOfferAcceptedPoints() { return OFFER_ACCEPTED_POINTS; }
} 