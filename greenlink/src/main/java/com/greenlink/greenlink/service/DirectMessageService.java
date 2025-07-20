package com.greenlink.greenlink.service;

import com.greenlink.greenlink.dto.DirectMessageConversationDto;
import com.greenlink.greenlink.dto.DirectMessageDto;
import com.greenlink.greenlink.model.DirectMessageConversation;
import com.greenlink.greenlink.model.User;

import java.util.List;

public interface DirectMessageService {
    
    // Conversation methods
    List<DirectMessageConversationDto> getUserConversations(User user);
    
    DirectMessageConversation getOrCreateConversation(User user1, User user2);
    
    DirectMessageConversation getConversationById(Long conversationId);
    
    // Message methods
    List<DirectMessageDto> getConversationMessages(Long conversationId, User currentUser);
    
    DirectMessageDto sendMessage(Long conversationId, User sender, String content);
    
    DirectMessageDto getMessageById(Long messageId, User currentUser);
    
    // Read status methods
    void markConversationAsRead(Long conversationId, User currentUser);
    
    long countUnreadMessages(User user);
    
    // Friend check
    boolean canSendMessage(User sender, User recipient);
} 