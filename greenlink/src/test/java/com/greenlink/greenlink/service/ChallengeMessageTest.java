package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.model.UserChallenge;
import java.util.List;
import com.greenlink.greenlink.repository.ChallengeRepository;
import com.greenlink.greenlink.repository.UserChallengeRepository;
import com.greenlink.greenlink.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ChallengeMessageTest {

    @Autowired
    private ChallengeTrackingService challengeTrackingService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserChallengeRepository userChallengeRepository;

    @Test
    public void testSendFirstMessageChallenge() {
        // Create a test user
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user = userRepository.save(user);

        // Find the "Send Your First Message" challenge
        List<Challenge> challenges = challengeRepository.findByTitle("Send Your First Message");
        Challenge messageChallenge;
        if (challenges.isEmpty()) {
            messageChallenge = new Challenge();
            messageChallenge.setTitle("Send Your First Message");
            messageChallenge.setDescription("The ecosystem thanks you for breaking the silence");
            messageChallenge.setPoints(10);
            messageChallenge.setBadge("Messenger of the Green Gods");
            messageChallenge.setType(Challenge.ChallengeType.DEFAULT_CHALLENGES);
            messageChallenge.setTargetValue(1);
            messageChallenge = challengeRepository.save(messageChallenge);
        } else {
            messageChallenge = challenges.get(0);
        }

        // Initially, the challenge should not be activated
        assertFalse(userChallengeRepository.findByUserIdAndChallengeId(user.getId(), messageChallenge.getId()).isPresent());

        // Track a message sent action
        challengeTrackingService.trackUserAction(user.getId(), "MESSAGE_SENT", null);

        // Check if the challenge was activated and completed
        var userChallenge = userChallengeRepository.findByUserIdAndChallengeId(user.getId(), messageChallenge.getId());
        assertTrue(userChallenge.isPresent());
        assertEquals(100, userChallenge.get().getProgressPercentage());
        assertEquals(Challenge.ChallengeStatus.COMPLETED, userChallenge.get().getStatus());
    }
} 