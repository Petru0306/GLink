package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.PointEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PointsService {
    
    /**
     * Add points to a user and track the event
     */
    PointEvent addPoints(Long userId, int points, String eventType, String description);
    
    /**
     * Add points to a user with related entity information
     */
    PointEvent addPoints(Long userId, int points, String eventType, String description, 
                        Long relatedEntityId, String relatedEntityType);
    
    /**
     * Get user's point history
     */
    List<PointEvent> getUserPointHistory(Long userId);
    
    /**
     * Get user's point history with pagination
     */
    Page<PointEvent> getUserPointHistory(Long userId, Pageable pageable);
    
    /**
     * Get user's point history by event type
     */
    List<PointEvent> getUserPointHistoryByEventType(Long userId, String eventType);
    
    /**
     * Get total points earned by user
     */
    int getTotalPointsEarned(Long userId);
    
    /**
     * Get total points earned by user for specific event type
     */
    int getTotalPointsByEventType(Long userId, String eventType);
    
    /**
     * Get user's level up count
     */
    long getLevelUpCount(Long userId);
    
    /**
     * Get user's recent point events
     */
    List<PointEvent> getRecentEvents(Long userId, int days);
    
    /**
     * Get user's event type statistics
     */
    Map<String, Long> getEventTypeStats(Long userId);
    
    /**
     * Get user's level up events
     */
    List<PointEvent> getLevelUpEvents(Long userId);
    
    /**
     * Award points for challenge completion
     */
    PointEvent awardChallengePoints(Long userId, Long challengeId, String challengeTitle, int points);
    
    /**
     * Award points for quiz completion
     */
    PointEvent awardQuizPoints(Long userId, Long quizId, String quizTitle, int points);
    
    /**
     * Award points for marketplace sale
     */
    PointEvent awardMarketplaceSalePoints(Long userId, Long productId, String productName, double saleAmount);
    
    /**
     * Award points for marketplace purchase
     */
    PointEvent awardMarketplacePurchasePoints(Long userId, Long productId, String productName, double purchaseAmount);
    
    /**
     * Award points for lesson completion
     */
    PointEvent awardLessonPoints(Long userId, Long lessonId, String lessonTitle, int points);
    
    /**
     * Award points for carbon calculator usage
     */
    PointEvent awardCarbonCalculatorPoints(Long userId, int points);
    
    /**
     * Award points for recycling map exploration
     */
    PointEvent awardRecyclingMapPoints(Long userId, int points);
    
    /**
     * Award points for profile update
     */
    PointEvent awardProfileUpdatePoints(Long userId, int points);
    
    /**
     * Award points for adding a friend
     */
    PointEvent awardFriendAddedPoints(Long userId, Long friendId, String friendName, int points);
    
    /**
     * Award points for sending a message
     */
    PointEvent awardMessageSentPoints(Long userId, Long conversationId, int points);
    
    /**
     * Award points for making an offer
     */
    PointEvent awardOfferMadePoints(Long userId, Long productId, String productName, double offerAmount, int points);
    
    /**
     * Award points for accepting an offer
     */
    PointEvent awardOfferAcceptedPoints(Long userId, Long productId, String productName, double acceptedAmount, int points);
} 