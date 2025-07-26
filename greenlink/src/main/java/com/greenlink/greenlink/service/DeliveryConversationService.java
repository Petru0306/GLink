package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Conversation;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        return conversationRepository.findByBuyerOrSellerAndStatusOrderByUpdatedAtDesc(
            user, user, Conversation.ConversationStatus.OPEN);
    }
    
    /**
     * Update delivery status
     */
    @Transactional
    public Conversation updateDeliveryStatus(Long conversationId, String newStatus) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        try {
            Conversation.DeliveryStatus status = Conversation.DeliveryStatus.valueOf(newStatus.toUpperCase());
            conversation.setDeliveryStatus(status);
            return conversationRepository.save(conversation);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid delivery status: " + newStatus);
        }
    }
    
    /**
     * Close delivery conversation
     */
    @Transactional
    public Conversation closeDeliveryConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        conversation.setStatus(Conversation.ConversationStatus.CLOSED);
        return conversationRepository.save(conversation);
    }
    
    /**
     * Complete delivery conversation
     */
    @Transactional
    public Conversation completeDeliveryConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        conversation.setStatus(Conversation.ConversationStatus.COMPLETED);
        conversation.setDeliveryStatus(Conversation.DeliveryStatus.COMPLETED);
        conversation.setDeliveryCompletedAt(LocalDateTime.now());
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