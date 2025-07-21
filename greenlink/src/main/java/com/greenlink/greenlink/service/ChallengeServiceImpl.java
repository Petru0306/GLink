package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.model.UserChallenge;
import com.greenlink.greenlink.repository.ChallengeRepository;
import com.greenlink.greenlink.repository.UserChallengeRepository;
import com.greenlink.greenlink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final UserRepository userRepository;

    @Autowired
    private MessageSource messageSource;

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
    public List<Challenge> getTranslatedChallenges() {
        List<Challenge> challenges = challengeRepository.findAll();
        Locale currentLocale = LocaleContextHolder.getLocale();
        
        for (Challenge challenge : challenges) {
            String translationKey = getTranslationKey(challenge.getTitle());
            if (translationKey != null) {
                try {
                    String translatedTitle = messageSource.getMessage(translationKey + ".title", null, currentLocale);
                    String translatedDescription = messageSource.getMessage(translationKey + ".description", null, currentLocale);
                    String translatedBadge = messageSource.getMessage(translationKey + ".badge", null, currentLocale);
                    
                    challenge.setTitle(translatedTitle);
                    challenge.setDescription(translatedDescription);
                    challenge.setBadge(translatedBadge);
                } catch (Exception e) {
                    // If translation not found, keep original values
                }
            }
        }
        
        return challenges;
    }

    private String getTranslationKey(String title) {
        // Map challenge titles to translation keys
        switch (title) {
            case "Use Carbon Calculator":
                return "challenge.use.carbon.calculator";
            case "Explore the Recycling Map":
                return "challenge.explore.recycling.map";
            case "Make an Offer on the Marketplace":
                return "challenge.make.offer.marketplace";
            case "Send Your First Message":
                return "challenge.send.first.message";
            case "Add an Item to Favorites":
                return "challenge.add.item.favorites";
            case "First Friend":
                return "challenge.first.friend";
            case "Three Friends":
                return "challenge.three.friends";
            case "Five Friends":
                return "challenge.five.friends";
            case "Ten Friends":
                return "challenge.ten.friends";
            case "Complete 1 Lesson":
                return "challenge.complete.1.lesson";
            case "Complete 3 Lessons":
                return "challenge.complete.3.lessons";
            case "Complete 6 Lessons":
                return "challenge.complete.6.lessons";
            case "List First Item":
                return "challenge.list.first.item";
            case "List 3 Items":
                return "challenge.list.3.items";
            case "List 5 Items":
                return "challenge.list.5.items";
            case "List 10 Items":
                return "challenge.list.10.items";
            case "List 15 Items":
                return "challenge.list.15.items";
            case "Buy First Item":
                return "challenge.buy.first.item";
            case "Buy 3 Items":
                return "challenge.buy.3.items";
            case "Buy 5 Items":
                return "challenge.buy.5.items";
            case "Buy 10 Items":
                return "challenge.buy.10.items";
            case "Buy 15 Items":
                return "challenge.buy.15.items";
            default:
                return null;
        }
    }

    @Override
    public List<UserChallenge> getTranslatedUserActiveChallenges(Long userId) {
        List<UserChallenge> userChallenges = userChallengeRepository.findByUserIdAndStatus(userId, Challenge.ChallengeStatus.IN_PROGRESS);
        return translateUserChallenges(userChallenges);
    }

    @Override
    public List<UserChallenge> getTranslatedUserCompletedChallenges(Long userId) {
        List<UserChallenge> userChallenges = userChallengeRepository.findByUserIdAndStatus(userId, Challenge.ChallengeStatus.COMPLETED);
        return translateUserChallenges(userChallenges);
    }

    @Override
    public List<UserChallenge> getUserActiveChallenges(Long userId) {
        return userChallengeRepository.findByUserIdAndStatus(userId, Challenge.ChallengeStatus.IN_PROGRESS);
    }

    @Override
    public List<UserChallenge> getUserCompletedChallenges(Long userId) {
        return userChallengeRepository.findByUserIdAndStatus(userId, Challenge.ChallengeStatus.COMPLETED);
    }

    private List<UserChallenge> translateUserChallenges(List<UserChallenge> userChallenges) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        
        for (UserChallenge userChallenge : userChallenges) {
            Challenge challenge = userChallenge.getChallenge();
            String translationKey = getTranslationKey(challenge.getTitle());
            if (translationKey != null) {
                try {
                    String translatedTitle = messageSource.getMessage(translationKey + ".title", null, currentLocale);
                    String translatedDescription = messageSource.getMessage(translationKey + ".description", null, currentLocale);
                    String translatedBadge = messageSource.getMessage(translationKey + ".badge", null, currentLocale);
                    
                    challenge.setTitle(translatedTitle);
                    challenge.setDescription(translatedDescription);
                    challenge.setBadge(translatedBadge);
                } catch (Exception e) {
                    // If translation not found, keep original values
                }
            }
        }
        
        return userChallenges;
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