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
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<Challenge> getChallengesByCategory(Challenge.ChallengeCategory category) {
        return challengeRepository.findByCategory(category);
    }

    @Override
    public List<UserChallenge> getUserChallenges(Long userId) {
        return userChallengeRepository.findByUserId(userId);
    }

    @Override
    public List<UserChallenge> getUserChallengesByStatus(Long userId, UserChallenge.ChallengeStatus status) {
        return userChallengeRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public Map<Challenge.ChallengeCategory, List<UserChallenge>> getUserChallengesByCategory(Long userId) {
        List<UserChallenge> userChallenges = getUserChallenges(userId);
        return userChallenges.stream()
                .collect(Collectors.groupingBy(uc -> uc.getChallenge().getCategory()));
    }

    @Override
    @Transactional
    public UserChallenge startChallenge(Long userId, Long challengeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        // Check if user already has this challenge
        List<UserChallenge> existingChallenges = userChallengeRepository.findByUserId(userId);
        boolean alreadyExists = existingChallenges.stream()
                .anyMatch(uc -> uc.getChallenge().getId().equals(challengeId));

        if (alreadyExists) {
            throw new RuntimeException("Challenge already started");
        }

        UserChallenge userChallenge = new UserChallenge(user, challenge);
        return userChallengeRepository.save(userChallenge);
    }

    @Override
    @Transactional
    public UserChallenge updateProgress(Long userId, Long challengeId, int progress) {
        UserChallenge userChallenge = userChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge progress not found"));

        if (!userChallenge.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to challenge");
        }

        userChallenge.setCurrentValue(progress);
        return userChallengeRepository.save(userChallenge);
    }

    @Override
    @Transactional
    public void updateProgressByEvent(Long userId, String event, int increment) {
        List<Challenge> challenges = challengeRepository.findByProgressEvent(event);
        List<UserChallenge> userChallenges = userChallengeRepository.findByUserId(userId);

        for (Challenge challenge : challenges) {
            UserChallenge userChallenge = userChallenges.stream()
                    .filter(uc -> uc.getChallenge().getId().equals(challenge.getId()))
                    .findFirst()
                    .orElse(null);

            if (userChallenge != null) {
                userChallenge.setCurrentValue(userChallenge.getCurrentValue() + increment);
                userChallengeRepository.save(userChallenge);
            }
        }
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

    @Override
    @Transactional
    public void createDefaultChallenges() {
        if (challengeRepository.count() == 0) {
            // Default Challenges
            challengeRepository.save(new Challenge(
                "Create Your Eco Avatar",
                "A weirdo from another planet who only eats leftovers",
                5,
                Challenge.ChallengeCategory.DEFAULT,
                "Captain Compostface",
                "🌱",
                1,
                "avatar_created"
            ));

            challengeRepository.save(new Challenge(
                "Complete Your First Lesson",
                "Understands plants better than people",
                10,
                Challenge.ChallengeCategory.DEFAULT,
                "The Leaf Whisperer",
                "📚",
                1,
                "lesson_completed"
            ));

            challengeRepository.save(new Challenge(
                "Upload Your First Photo",
                "Snaps pics faster than trash hits the ground",
                10,
                Challenge.ChallengeCategory.DEFAULT,
                "Shutter the Litter",
                "📸",
                1,
                "photo_uploaded"
            ));

            challengeRepository.save(new Challenge(
                "Take the Eco Personality Quiz",
                "Thinks Buzzfeed quizzes are spiritual experiences",
                10,
                Challenge.ChallengeCategory.DEFAULT,
                "The Sorting Sprout",
                "🧠",
                1,
                "quiz_completed"
            ));

            challengeRepository.save(new Challenge(
                "First Item Listed",
                "Lives for shiny reusable objects",
                15,
                Challenge.ChallengeCategory.DEFAULT,
                "Eco-Barter Goblin",
                "🏷️",
                1,
                "item_listed"
            ));

            challengeRepository.save(new Challenge(
                "Use Carbon Calculator",
                "Knows what size carbon shoe you wear",
                10,
                Challenge.ChallengeCategory.DEFAULT,
                "Footprint Prophet",
                "👣",
                1,
                "carbon_calculated"
            ));

            challengeRepository.save(new Challenge(
                "Explore the Recycling Map",
                "Opened the ancient map of hidden recycling lore",
                10,
                Challenge.ChallengeCategory.DEFAULT,
                "The Bin Seeker",
                "🗺️",
                1,
                "map_explored"
            ));

            challengeRepository.save(new Challenge(
                "Make an Offer on the Marketplace",
                "Bartered like it's the Shire Flea Market",
                10,
                Challenge.ChallengeCategory.DEFAULT,
                "The Haggle Hobbit",
                "💰",
                1,
                "offer_made"
            ));

            challengeRepository.save(new Challenge(
                "Send Your First Message",
                "The ecosystem thanks you for breaking the silence",
                10,
                Challenge.ChallengeCategory.DEFAULT,
                "Messenger of the Green Gods",
                "💬",
                1,
                "message_sent"
            ));

            challengeRepository.save(new Challenge(
                "Add an Item to Favorites",
                "You liked it, so you should've put a leaf on it",
                5,
                Challenge.ChallengeCategory.DEFAULT,
                "Heart of Cartness",
                "❤️",
                1,
                "item_favorited"
            ));

            // Ambassador Challenges
            challengeRepository.save(new Challenge(
                "First Friend",
                "Probably just messaged you",
                10,
                Challenge.ChallengeCategory.AMBASSADOR,
                "The Green Recruiter",
                "🤝",
                1,
                "friend_added"
            ));

            challengeRepository.save(new Challenge(
                "Three Friends",
                "Can't stop talking about compost at parties",
                15,
                Challenge.ChallengeCategory.AMBASSADOR,
                "Sir Shares-a-Lot",
                "👥",
                3,
                "friend_added"
            ));

            challengeRepository.save(new Challenge(
                "Five Friends",
                "Granting green wishes one DM at a time",
                25,
                Challenge.ChallengeCategory.AMBASSADOR,
                "Influensir",
                "🌟",
                5,
                "friend_added"
            ));

            challengeRepository.save(new Challenge(
                "Ten Friends",
                "Rules the realm of referrals",
                50,
                Challenge.ChallengeCategory.AMBASSADOR,
                "The Sustainabili-Tyrant",
                "👑",
                10,
                "friend_added"
            ));

            challengeRepository.save(new Challenge(
                "Fifteen Friends",
                "Fifteen converts and counting",
                75,
                Challenge.ChallengeCategory.AMBASSADOR,
                "The Eco Cult Leader",
                "🔥",
                15,
                "friend_added"
            ));

            // Maester Challenges
            challengeRepository.save(new Challenge(
                "Complete 1 Lesson",
                "Just left the Shire with a reusable cup",
                10,
                Challenge.ChallengeCategory.MAESTER,
                "The Green Hobbit",
                "📖",
                1,
                "lesson_completed"
            ));

            challengeRepository.save(new Challenge(
                "Complete 3 Lessons",
                "Carries a wand made of cardboard tubes",
                30,
                Challenge.ChallengeCategory.MAESTER,
                "Recyclas the Wise",
                "🧙‍♂️",
                3,
                "lesson_completed"
            ));

            challengeRepository.save(new Challenge(
                "Complete 6 Lessons",
                "Always knows what bait to use for eco-activism",
                50,
                Challenge.ChallengeCategory.MAESTER,
                "Master Baitor",
                "🎣",
                6,
                "lesson_completed"
            ));

            // Shelf Whisperer Challenges
            challengeRepository.save(new Challenge(
                "List First Item",
                "Found treasure in a pile of old Tupperware",
                20,
                Challenge.ChallengeCategory.SHELF_WHISPERER,
                "Thrift Gremlin",
                "🏪",
                1,
                "item_listed"
            ));

            challengeRepository.save(new Challenge(
                "List 3 Items",
                "Part dragon, part Facebook Marketplace admin",
                35,
                Challenge.ChallengeCategory.SHELF_WHISPERER,
                "Dealzard",
                "🐉",
                3,
                "item_listed"
            ));

            challengeRepository.save(new Challenge(
                "List 5 Items",
                "Sells only reused jewelry",
                75,
                Challenge.ChallengeCategory.SHELF_WHISPERER,
                "Lord of the Re-Rings",
                "💍",
                5,
                "item_listed"
            ));

            challengeRepository.save(new Challenge(
                "List 10 Items",
                "Hasn't bought anything new since 2018",
                100,
                Challenge.ChallengeCategory.SHELF_WHISPERER,
                "The Resale Rogue",
                "🦹‍♂️",
                10,
                "item_listed"
            ));

            challengeRepository.save(new Challenge(
                "List 15 Items",
                "Reigns supreme in the secondhand kingdom",
                150,
                Challenge.ChallengeCategory.SHELF_WHISPERER,
                "King Cash-for-Trash",
                "👑",
                15,
                "item_listed"
            ));

            // Cart Goblin Challenges
            challengeRepository.save(new Challenge(
                "Buy First Item",
                "Finds deals greener than your kale smoothie",
                20,
                Challenge.ChallengeCategory.CART_GOBLIN,
                "Bargain Broccoli",
                "🥦",
                1,
                "item_purchased"
            ));

            challengeRepository.save(new Challenge(
                "Buy 3 Items",
                "Shops only during moon cycles and zero-waste sales",
                35,
                Challenge.ChallengeCategory.CART_GOBLIN,
                "The Ethical Witch",
                "🧙‍♀️",
                3,
                "item_purchased"
            ));

            challengeRepository.save(new Challenge(
                "Buy 5 Items",
                "Flies from cart to cart, pecking eco deals",
                75,
                Challenge.ChallengeCategory.CART_GOBLIN,
                "Swipe Sparrow",
                "🐦",
                5,
                "item_purchased"
            ));

            challengeRepository.save(new Challenge(
                "Buy 10 Items",
                "Sees through fake eco marketing like Neo in the Matrix",
                100,
                Challenge.ChallengeCategory.CART_GOBLIN,
                "Greenwashing Slayer",
                "🕶️",
                10,
                "item_purchased"
            ));

            challengeRepository.save(new Challenge(
                "Buy 15 Items",
                "No plastic survives their scroll",
                150,
                Challenge.ChallengeCategory.CART_GOBLIN,
                "The Cart Crusader",
                "⚔️",
                15,
                "item_purchased"
            ));
        }
    }

    @Override
    @Transactional
    public void initializeUserChallenges(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Challenge> allChallenges = getAllChallenges();
        List<UserChallenge> existingChallenges = getUserChallenges(userId);

        for (Challenge challenge : allChallenges) {
            boolean alreadyExists = existingChallenges.stream()
                    .anyMatch(uc -> uc.getChallenge().getId().equals(challenge.getId()));

            if (!alreadyExists) {
                UserChallenge userChallenge = new UserChallenge(user, challenge);
                userChallengeRepository.save(userChallenge);
            }
        }
    }
} 