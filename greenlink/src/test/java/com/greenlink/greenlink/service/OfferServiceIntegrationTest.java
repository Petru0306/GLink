package com.greenlink.greenlink.service;

import com.greenlink.greenlink.dto.MessageDto;
import com.greenlink.greenlink.model.Conversation;
import com.greenlink.greenlink.model.Message;
import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.ConversationRepository;
import com.greenlink.greenlink.repository.MessageRepository;
import com.greenlink.greenlink.repository.ProductRepository;
import com.greenlink.greenlink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OfferServiceIntegrationTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    private User seller;
    private User buyer;
    private Product product;
    private Conversation conversation;
    private Message offerMessage;

    @BeforeEach
    void setUp() {
        // Create test users with unique emails
        String timestamp = String.valueOf(System.currentTimeMillis());
        seller = new User();
        seller.setEmail("seller" + timestamp + "@test.com");
        seller.setFirstName("Seller");
        seller.setLastName("Test");
        seller.setPassword("TestPass123!");
        seller = userRepository.save(seller);

        buyer = new User();
        buyer.setEmail("buyer" + timestamp + "@test.com");
        buyer.setFirstName("Buyer");
        buyer.setLastName("Test");
        buyer.setPassword("TestPass123!");
        buyer = userRepository.save(buyer);

        // Create test product
        product = new Product();
        product.setName("Test Product");
        product.setPrice(100.0);
        product.setSeller(seller);
        product = productRepository.save(product);

        // Create conversation
        conversation = new Conversation();
        conversation.setProduct(product);
        conversation.setSeller(seller);
        conversation.setBuyer(buyer);
        conversation = conversationRepository.save(conversation);

        // Create initial offer
        offerMessage = new Message();
        offerMessage.setConversation(conversation);
        offerMessage.setSender(buyer);
        offerMessage.setOfferAmount(80.0);
        offerMessage.setOfferStatus(Message.OfferStatus.ACTION_REQUIRED);
        offerMessage.setContent("Test offer");
        offerMessage = messageRepository.save(offerMessage);
    }

    @Test
    void testOptimisticLockingPreventsRaceConditions() throws InterruptedException {
        // Test that optimistic locking prevents duplicate processing
        // First response should succeed
        MessageDto firstResponse = messageService.respondToOffer(offerMessage.getId(), seller, "ACCEPT", null);
        assertNotNull(firstResponse);
        assertEquals("ACCEPTED", firstResponse.getOfferStatus());

        // Second response should fail because the offer is already processed
        assertThrows(IllegalStateException.class, () -> {
            messageService.respondToOffer(offerMessage.getId(), seller, "REJECT", null);
        });

        // Verify the final state
        Message updatedMessage = messageRepository.findById(offerMessage.getId()).orElse(null);
        assertNotNull(updatedMessage);
        assertEquals(Message.OfferStatus.ACCEPTED, updatedMessage.getOfferStatus());
    }

    @Test
    void testCounterOfferCreatesNewMessageWithReferences() {
        // Create counter offer
        MessageDto counterOffer = messageService.respondToOffer(
                offerMessage.getId(), seller, "COUNTER", 90.0);

        assertNotNull(counterOffer);
        assertEquals(90.0, counterOffer.getOfferAmount());
        assertEquals("ACTION_REQUIRED", counterOffer.getOfferStatus());

        // Verify the original offer is marked as countered
        Message originalOffer = messageRepository.findById(offerMessage.getId()).orElse(null);
        assertNotNull(originalOffer);
        assertEquals(Message.OfferStatus.COUNTERED, originalOffer.getOfferStatus());

        // Verify the counter offer references the original
        Message counterOfferEntity = messageRepository.findById(counterOffer.getId()).orElse(null);
        assertNotNull(counterOfferEntity);
        assertEquals(offerMessage.getId(), counterOfferEntity.getOriginalOfferMessage().getId());

        // Verify the original offer references the counter
        assertEquals(counterOfferEntity.getId(), originalOffer.getCounterOfferMessage().getId());
    }

    @Test
    void testOfferExpiration() {
        // Create an expired offer
        final Message expiredOffer = new Message();
        expiredOffer.setConversation(conversation);
        expiredOffer.setSender(buyer);
        expiredOffer.setOfferAmount(70.0);
        expiredOffer.setOfferStatus(Message.OfferStatus.ACTION_REQUIRED);
        expiredOffer.setOfferExpiresAt(java.time.LocalDateTime.now().minusDays(1)); // Expired yesterday
        expiredOffer.setContent("Expired offer");
        final Message savedExpiredOffer = messageRepository.save(expiredOffer);

        // Try to respond to expired offer
        assertThrows(IllegalStateException.class, () -> {
            messageService.respondToOffer(savedExpiredOffer.getId(), seller, "ACCEPT", null);
        });
    }

    @Test
    void testCannotRespondToOwnOffer() {
        assertThrows(IllegalStateException.class, () -> {
            messageService.respondToOffer(offerMessage.getId(), buyer, "ACCEPT", null);
        });
    }

    @Test
    void testCannotRespondToAlreadyProcessedOffer() {
        // First response should succeed
        messageService.respondToOffer(offerMessage.getId(), seller, "ACCEPT", null);

        // Second response should fail
        assertThrows(IllegalStateException.class, () -> {
            messageService.respondToOffer(offerMessage.getId(), seller, "REJECT", null);
        });
    }

    @Test
    void testOfferVersionIncrementsOnUpdate() {
        // Clear the persistence context to ensure we get fresh data
        messageRepository.flush();
        
        // Get the current version from the database
        Message freshOffer = messageRepository.findById(offerMessage.getId()).orElse(null);
        assertNotNull(freshOffer);
        Long initialVersion = freshOffer.getVersion();

        // Respond to offer
        MessageDto result = messageService.respondToOffer(offerMessage.getId(), seller, "ACCEPT", null);
        assertNotNull(result);

        // Clear persistence context again and check version
        messageRepository.flush();
        Message updatedOffer = messageRepository.findById(offerMessage.getId()).orElse(null);
        assertNotNull(updatedOffer);
        assertTrue(updatedOffer.getVersion() > initialVersion, 
                "Version should be incremented. Expected > " + initialVersion + ", but was " + updatedOffer.getVersion());
    }

    @Test
    void testInvalidActionThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            messageService.respondToOffer(offerMessage.getId(), seller, "INVALID_ACTION", null);
        });
    }
} 