package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.ChallengeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChallengeService {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserService userService;

    public List<Challenge> getUserActiveChallenges(Long userId) {
        return challengeRepository.findByUserIdAndStatus(userId, Challenge.ChallengeStatus.IN_PROGRESS);
    }

    public List<Challenge> getUserCompletedChallenges(Long userId) {
        return challengeRepository.findByUserIdAndStatus(userId, Challenge.ChallengeStatus.COMPLETED);
    }

    public Challenge completeChallenge(Long challengeId, Long userId) {
        Challenge challenge = challengeRepository.findByIdAndUserId(challengeId, userId)
                .orElseThrow(() -> new RuntimeException("Provocare negăsită"));

        if (challenge.getStatus() == Challenge.ChallengeStatus.COMPLETED) {
            throw new RuntimeException("Provocarea a fost deja completată");
        }

        challenge.setStatus(Challenge.ChallengeStatus.COMPLETED);
        challenge.setCompletedAt(LocalDateTime.now());

        // Adaugă punctele la profilul utilizatorului
        userService.addPoints(userId, challenge.getPointsValue());

        return challengeRepository.save(challenge);
    }

    public Challenge createChallenge(Challenge challenge) {
        challenge.setStartDate(LocalDateTime.now());
        challenge.setStatus(Challenge.ChallengeStatus.IN_PROGRESS);
        return challengeRepository.save(challenge);
    }
}