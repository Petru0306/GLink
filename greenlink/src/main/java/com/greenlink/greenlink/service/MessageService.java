package com.greenlink.greenlink.service;

import com.greenlink.greenlink.dto.ConversationDto;
import com.greenlink.greenlink.dto.MessageDto;
import com.greenlink.greenlink.model.Conversation;
import com.greenlink.greenlink.model.User;

import java.util.List;

public interface MessageService {
    
    // Conversation methods
    List<ConversationDto> getUserConversations(User user);
    
    Conversation getOrCreateConversation(Long productId, User currentUser);
    
    Conversation getConversationById(Long conversationId);
    
    // Message methods
    List<MessageDto> getConversationMessages(Long conversationId, User currentUser);
    
    MessageDto sendMessage(Long conversationId, User sender, String content);
    
    MessageDto makeOffer(Long conversationId, User sender, Double offerAmount);
    
    MessageDto respondToOffer(Long messageId, User responder, String action, Double counterOfferAmount);
    
    MessageDto getMessageById(Long messageId, User currentUser);
    
    // Read status methods
    void markConversationAsRead(Long conversationId, User currentUser);
    
    long countUnreadMessages(User user);
} 