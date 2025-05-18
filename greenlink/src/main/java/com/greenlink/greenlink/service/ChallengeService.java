package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.UserChallenge;
import java.util.List;

public interface ChallengeService {
    List<Challenge> getAllChallenges();
    List<Challenge> getChallengesByType(Challenge.ChallengeType type);
    List<UserChallenge> getUserActiveChallenges(Long userId);
    List<UserChallenge> getUserCompletedChallenges(Long userId);
    UserChallenge startChallenge(Long userId, Long challengeId);
    UserChallenge updateProgress(Long userId, Long challengeId, int progress);
    long getCompletedChallengesCount(Long userId);
    int getCurrentStreak(Long userId);
    int getTotalPoints(Long userId);
    void createDefaultChallenges();
}