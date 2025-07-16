package com.greenlink.greenlink.config;

import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.repository.ChallengeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializationConfig implements CommandLineRunner {

    @Autowired
    private ChallengeRepository challengeRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        dropConstraintIfNeeded();
        addBadgeColumnIfNeeded();
        updateOldChallengeTypes();
        createDefaultChallengesIfNeeded();
    }

    private void dropConstraintIfNeeded() {
        try {
            jdbcTemplate.execute("ALTER TABLE challenges DROP CONSTRAINT IF EXISTS challenges_type_check;");
        } catch (Exception e) {
            // Log the exception if necessary, but continue execution
            System.err.println("Could not drop constraint: " + e.getMessage());
        }
    }

    private void addBadgeColumnIfNeeded() {
        try {
            jdbcTemplate.execute("ALTER TABLE challenges ADD COLUMN IF NOT EXISTS badge VARCHAR(100);");
        } catch (Exception e) {
            // Log the exception if necessary, but continue execution
            System.err.println("Could not add badge column: " + e.getMessage());
        }
    }

    private void updateOldChallengeTypes() {
        // Clear all existing challenges to replace with new ones
        jdbcTemplate.execute("DELETE FROM user_challenges");
        jdbcTemplate.execute("DELETE FROM challenges");
    }

    private void createDefaultChallengesIfNeeded() {
        if (challengeRepository.count() == 0) {
            // Default Challenges
            challengeRepository.save(new Challenge(
                "Create Your Eco Avatar",
                "A weirdo from another planet who only eats leftovers",
                5,
                "Captain Compostface",
                Challenge.ChallengeType.DEFAULT_CHALLENGES
            ));

            challengeRepository.save(new Challenge(
                "Complete Your First Lesson",
                "Understands plants better than people",
                10,
                "The Leaf Whisperer",
                Challenge.ChallengeType.DEFAULT_CHALLENGES
            ));

            challengeRepository.save(new Challenge(
                "Upload Your First Photo",
                "Snaps pics faster than trash hits the ground",
                10,
                "Shutter the Litter",
                Challenge.ChallengeType.DEFAULT_CHALLENGES
            ));

            challengeRepository.save(new Challenge(
                "Take the Eco Personality Quiz",
                "Thinks Buzzfeed quizzes are spiritual experiences",
                10,
                "The Sorting Sprout",
                Challenge.ChallengeType.DEFAULT_CHALLENGES
            ));

            challengeRepository.save(new Challenge(
                "First Item Listed",
                "Lives for shiny reusable objects",
                15,
                "Eco-Barter Goblin",
                Challenge.ChallengeType.DEFAULT_CHALLENGES
            ));

            challengeRepository.save(new Challenge(
                "Use Carbon Calculator",
                "Knows what size carbon shoe you wear",
                10,
                "Footprint Prophet",
                Challenge.ChallengeType.DEFAULT_CHALLENGES
            ));

            challengeRepository.save(new Challenge(
                "Explore the Recycling Map",
                "Opened the ancient map of hidden recycling lore",
                10,
                "The Bin Seeker",
                Challenge.ChallengeType.DEFAULT_CHALLENGES
            ));

            challengeRepository.save(new Challenge(
                "Make an Offer on the Marketplace",
                "Bartered like it's the Shire Flea Market",
                10,
                "The Haggle Hobbit",
                Challenge.ChallengeType.DEFAULT_CHALLENGES
            ));

            challengeRepository.save(new Challenge(
                "Send Your First Message",
                "The ecosystem thanks you for breaking the silence",
                10,
                "Messenger of the Green Gods",
                Challenge.ChallengeType.DEFAULT_CHALLENGES
            ));

            challengeRepository.save(new Challenge(
                "Add an Item to Favorites",
                "You liked it, so you should've put a leaf on it",
                5,
                "Heart of Cartness",
                Challenge.ChallengeType.DEFAULT_CHALLENGES
            ));

            // Ambassador Challenges (Referrals)
            challengeRepository.save(new Challenge(
                "First Friend",
                "Probably just messaged you",
                10,
                "The Green Recruiter",
                Challenge.ChallengeType.AMBASSADOR
            ));

            challengeRepository.save(new Challenge(
                "Three Friends",
                "Can't stop talking about compost at parties",
                15,
                "Sir Shares-a-Lot",
                Challenge.ChallengeType.AMBASSADOR
            ));

            challengeRepository.save(new Challenge(
                "Five Friends",
                "Granting green wishes one DM at a time",
                25,
                "Influensir",
                Challenge.ChallengeType.AMBASSADOR
            ));

            challengeRepository.save(new Challenge(
                "Ten Friends",
                "Rules the realm of referrals",
                50,
                "The Sustainabili-Tyrant",
                Challenge.ChallengeType.AMBASSADOR
            ));

            challengeRepository.save(new Challenge(
                "Fifteen Friends",
                "Fifteen converts and counting",
                75,
                "The Eco Cult Leader",
                Challenge.ChallengeType.AMBASSADOR
            ));

            // Maester Challenges (Lessons)
            challengeRepository.save(new Challenge(
                "Complete 1 Lesson",
                "Just left the Shire with a reusable cup",
                10,
                "The Green Hobbit",
                Challenge.ChallengeType.MAESTER
            ));

            challengeRepository.save(new Challenge(
                "Complete 3 Lessons",
                "Carries a wand made of cardboard tubes",
                30,
                "Recyclas the Wise",
                Challenge.ChallengeType.MAESTER
            ));

            challengeRepository.save(new Challenge(
                "Complete 6 Lessons",
                "Always knows what bait to use for eco-activism",
                50,
                "Master Baitor",
                Challenge.ChallengeType.MAESTER
            ));

            // Shelf Whisperer Challenges (Marketplace Listings)
            challengeRepository.save(new Challenge(
                "List First Item",
                "Found treasure in a pile of old Tupperware",
                20,
                "Thrift Gremlin",
                Challenge.ChallengeType.SHELF_WHISPERER
            ));

            challengeRepository.save(new Challenge(
                "List 3 Items",
                "Part dragon, part Facebook Marketplace admin",
                35,
                "Dealzard",
                Challenge.ChallengeType.SHELF_WHISPERER
            ));

            challengeRepository.save(new Challenge(
                "List 5 Items",
                "Sells only reused jewelry",
                75,
                "Lord of the Re-Rings",
                Challenge.ChallengeType.SHELF_WHISPERER
            ));

            challengeRepository.save(new Challenge(
                "List 10 Items",
                "Hasn't bought anything new since 2018",
                100,
                "The Resale Rogue",
                Challenge.ChallengeType.SHELF_WHISPERER
            ));

            challengeRepository.save(new Challenge(
                "List 15 Items",
                "Reigns supreme in the secondhand kingdom",
                150,
                "King Cash-for-Trash",
                Challenge.ChallengeType.SHELF_WHISPERER
            ));

            // Cart Goblin Challenges (Marketplace Purchases)
            challengeRepository.save(new Challenge(
                "Buy First Item",
                "Finds deals greener than your kale smoothie",
                20,
                "Bargain Broccoli",
                Challenge.ChallengeType.CART_GOBLIN
            ));

            challengeRepository.save(new Challenge(
                "Buy 3 Items",
                "Shops only during moon cycles and zero-waste sales",
                35,
                "The Ethical Witch",
                Challenge.ChallengeType.CART_GOBLIN
            ));

            challengeRepository.save(new Challenge(
                "Buy 5 Items",
                "Flies from cart to cart, pecking eco deals",
                75,
                "Swipe Sparrow",
                Challenge.ChallengeType.CART_GOBLIN
            ));

            challengeRepository.save(new Challenge(
                "Buy 10 Items",
                "Sees through fake eco marketing like Neo in the Matrix",
                100,
                "Greenwashing Slayer",
                Challenge.ChallengeType.CART_GOBLIN
            ));

            challengeRepository.save(new Challenge(
                "Buy 15 Items",
                "No plastic survives their scroll",
                150,
                "The Cart Crusader",
                Challenge.ChallengeType.CART_GOBLIN
            ));
        }
    }
} 