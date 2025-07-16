package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.model.UserChallenge;
import com.greenlink.greenlink.repository.ChallengeRepository;
import com.greenlink.greenlink.repository.UserChallengeRepository;
import com.greenlink.greenlink.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final UserRepository userRepository;

    public ChallengeServiceImpl(ChallengeRepository challengeRepository,
                              UserChallengeRepository userChallengeRepository,
                              UserRepository userRepository) {
        this.challengeRepository = challengeRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    @Override
    public List<Challenge> getChallengesByType(Challenge.ChallengeType type) {
        return challengeRepository.findByType(type);
    }

    @Override
    public List<UserChallenge> getUserActiveChallenges(Long userId) {
        return userChallengeRepository.findByUserIdAndStatus(userId, Challenge.ChallengeStatus.IN_PROGRESS);
    }

    @Override
    public List<UserChallenge> getUserCompletedChallenges(Long userId) {
        return userChallengeRepository.findByUserIdAndStatus(userId, Challenge.ChallengeStatus.COMPLETED);
    }

    @Override
    @Transactional
    public UserChallenge startChallenge(Long userId, Long challengeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        UserChallenge userChallenge = new UserChallenge(user, challenge);
        return userChallengeRepository.save(userChallenge);
    }

    @Override
    @Transactional
    public UserChallenge updateProgress(Long userId, Long userChallengeId, int progress) {
        UserChallenge userChallenge = userChallengeRepository.findById(userChallengeId)
                .orElseThrow(() -> new RuntimeException("Challenge progress not found"));

        if (!userChallenge.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to challenge");
        }

        userChallenge.setProgressPercentage(progress);
        return userChallengeRepository.save(userChallenge);
    }

    @Override
    public long getCompletedChallengesCount(Long userId) {
        return userChallengeRepository.countCompletedChallengesByUserId(userId);
    }

    @Override
    public int getCurrentStreak(Long userId) {
        return userChallengeRepository.getCurrentStreak(userId);
    }

    @Override
    public int getTotalPoints(Long userId) {
        Integer points = userChallengeRepository.getTotalPoints(userId);
        return points != null ? points : 0;
    }
} 