package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.UserChallenge;
import java.util.List;
import java.util.Map;

public interface ChallengeService {
    List<Challenge> getAllChallenges();
    List<Challenge> getChallengesByCategory(Challenge.ChallengeCategory category);
    List<UserChallenge> getUserChallenges(Long userId);
    List<UserChallenge> getUserChallengesByStatus(Long userId, UserChallenge.ChallengeStatus status);
    Map<Challenge.ChallengeCategory, List<UserChallenge>> getUserChallengesByCategory(Long userId);
    UserChallenge startChallenge(Long userId, Long challengeId);
    UserChallenge updateProgress(Long userId, Long challengeId, int progress);
    void updateProgressByEvent(Long userId, String event, int increment);
    long getCompletedChallengesCount(Long userId);
    int getCurrentStreak(Long userId);
    int getTotalPoints(Long userId);
    void createDefaultChallenges();
    void initializeUserChallenges(Long userId);
}