package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Conversation;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeliveryConversationService {
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    /**
     * Get all delivery conversations for a user (as buyer or seller)
     */
    public List<Conversation> getDeliveryConversationsForUser(User user) {
        return conversationRepository.findByBuyerOrSellerOrderByUpdatedAtDesc(user, user);
    }
    
    /**
     * Get active delivery conversations for a user
     */
    public List<Conversation> getActiveDeliveryConversationsForUser(User user) {
        // Temporarily return all conversations until migration works
        return conversationRepository.findByBuyerOrSellerOrderByUpdatedAtDesc(user, user);
    }
    
    /**
     * Update delivery status
     */
    @Transactional
    public Conversation updateDeliveryStatus(Long conversationId, String newStatus) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        // Temporarily simplified until migration works
        return conversationRepository.save(conversation);
    }
    
    /**
     * Close delivery conversation
     */
    @Transactional
    public Conversation closeDeliveryConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        // Temporarily simplified until migration works
        return conversationRepository.save(conversation);
    }
    
    /**
     * Complete delivery conversation
     */
    @Transactional
    public Conversation completeDeliveryConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        // Temporarily simplified until migration works
        return conversationRepository.save(conversation);
    }
    
    /**
     * Get delivery conversation by product ID
     */
    public Conversation getDeliveryConversationByProductId(Long productId) {
        return conversationRepository.findByProductId(productId)
                .orElse(null);
    }
} 