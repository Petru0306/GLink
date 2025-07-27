package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.ChallengeRepository;
import com.greenlink.greenlink.repository.ProductRepository;
import com.greenlink.greenlink.repository.UserChallengeRepository;
import com.greenlink.greenlink.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ChallengeBuyTest {

    @Autowired
    private ChallengeTrackingService challengeTrackingService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserChallengeRepository userChallengeRepository;

    @Test
    public void testBuyFirstItemChallenge() {
        // Create test users
        User seller = new User();
        seller.setEmail("seller@example.com");
        seller.setPassword("Password123!");
        seller.setFirstName("Seller");
        seller.setLastName("User");
        seller = userRepository.save(seller);

        User buyer = new User();
        buyer.setEmail("buyer@example.com");
        buyer.setPassword("Password123!");
        buyer.setFirstName("Buyer");
        buyer.setLastName("User");
        buyer = userRepository.save(buyer);

        // Create a test product
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("A test product");
        product.setPrice(100.0);
        product.setSeller(seller);
        product.setSold(false);
        product = productRepository.save(product);

        // Find the "Buy First Item" challenge
        List<Challenge> challenges = challengeRepository.findByTitle("Buy First Item");
        Challenge buyChallenge;
        if (challenges.isEmpty()) {
            buyChallenge = new Challenge();
            buyChallenge.setTitle("Buy First Item");
            buyChallenge.setDescription("Finds deals greener than your kale smoothie");
            buyChallenge.setPoints(10);
            buyChallenge.setBadge("Bargain Broccoli");
            buyChallenge.setType(Challenge.ChallengeType.DEFAULT_CHALLENGES);
            buyChallenge.setTargetValue(1);
            buyChallenge = challengeRepository.save(buyChallenge);
        } else {
            buyChallenge = challenges.get(0);
        }

        // Initially, the challenge should not be activated
        assertFalse(userChallengeRepository.findByUserIdAndChallengeId(buyer.getId(), buyChallenge.getId()).isPresent());

        // Simulate a purchase by marking the product as sold
        product.setSold(true);
        product.setBuyer(buyer);
        product.setSoldAt(LocalDateTime.now());
        productRepository.save(product);

        // Track the purchase action
        challengeTrackingService.trackUserAction(buyer.getId(), "MARKETPLACE_ITEM_PURCHASED", product.getId());

        // Check if the challenge was activated and completed
        var userChallenge = userChallengeRepository.findByUserIdAndChallengeId(buyer.getId(), buyChallenge.getId());
        assertTrue(userChallenge.isPresent());
        assertEquals(100, userChallenge.get().getProgressPercentage());
        assertEquals(Challenge.ChallengeStatus.COMPLETED, userChallenge.get().getStatus());
    }

    @Test
    public void testBuyMultipleItemsChallenge() {
        // Create test users
        User seller = new User();
        seller.setEmail("seller2@example.com");
        seller.setPassword("Password123!");
        seller.setFirstName("Seller");
        seller.setLastName("User");
        seller = userRepository.save(seller);

        User buyer = new User();
        buyer.setEmail("buyer2@example.com");
        buyer.setPassword("Password123!");
        buyer.setFirstName("Buyer");
        buyer.setLastName("User");
        buyer = userRepository.save(buyer);

        // Find the "Buy 3 Items" challenge
        List<Challenge> challenges = challengeRepository.findByTitle("Buy 3 Items");
        Challenge buyChallenge;
        if (challenges.isEmpty()) {
            buyChallenge = new Challenge();
            buyChallenge.setTitle("Buy 3 Items");
            buyChallenge.setDescription("Shops only during moon cycles and zero-waste sales");
            buyChallenge.setPoints(25);
            buyChallenge.setBadge("The Ethical Witch");
            buyChallenge.setType(Challenge.ChallengeType.DEFAULT_CHALLENGES);
            buyChallenge.setTargetValue(3);
            buyChallenge = challengeRepository.save(buyChallenge);
        } else {
            buyChallenge = challenges.get(0);
        }

        // Initially, the challenge should not be activated
        assertFalse(userChallengeRepository.findByUserIdAndChallengeId(buyer.getId(), buyChallenge.getId()).isPresent());

        // Create and purchase 3 products
        for (int i = 1; i <= 3; i++) {
            Product product = new Product();
            product.setName("Test Product " + i);
            product.setDescription("A test product " + i);
            product.setPrice(100.0 + i);
            product.setSeller(seller);
            product.setSold(false);
            product = productRepository.save(product);

            // Simulate purchase
            product.setSold(true);
            product.setBuyer(buyer);
            product.setSoldAt(LocalDateTime.now());
            productRepository.save(product);

            // Track the purchase action
            challengeTrackingService.trackUserAction(buyer.getId(), "MARKETPLACE_ITEM_PURCHASED", product.getId());
        }

        // Check if the challenge was activated and completed
        var userChallenge = userChallengeRepository.findByUserIdAndChallengeId(buyer.getId(), buyChallenge.getId());
        assertTrue(userChallenge.isPresent());
        assertEquals(100, userChallenge.get().getProgressPercentage());
        assertEquals(Challenge.ChallengeStatus.COMPLETED, userChallenge.get().getStatus());
    }
} 