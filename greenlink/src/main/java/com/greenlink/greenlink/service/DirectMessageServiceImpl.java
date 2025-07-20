package com.greenlink.greenlink.service;

import com.greenlink.greenlink.dto.DirectMessageConversationDto;
import com.greenlink.greenlink.dto.DirectMessageDto;
import com.greenlink.greenlink.model.DirectMessage;
import com.greenlink.greenlink.model.DirectMessageConversation;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.DirectMessageConversationRepository;
import com.greenlink.greenlink.repository.DirectMessageRepository;
import com.greenlink.greenlink.repository.FriendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DirectMessageServiceImpl implements DirectMessageService {

    private final DirectMessageConversationRepository conversationRepository;
    private final DirectMessageRepository messageRepository;
    private final FriendRepository friendRepository;
    
    @Autowired
    private SystemMessageService systemMessageService;

    public DirectMessageServiceImpl(DirectMessageConversationRepository conversationRepository,
                                   DirectMessageRepository messageRepository,
                                   FriendRepository friendRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.friendRepository = friendRepository;
    }

    @Override
    public List<DirectMessageConversationDto> getUserConversations(User user) {
        List<DirectMessageConversation> conversations = conversationRepository.findConversationsByUser(user);
        return conversations.stream()
                .map(conversation -> {
                    DirectMessageConversationDto dto = DirectMessageConversationDto.fromEntity(conversation, user);
                    // Load messages for each conversation
                    List<DirectMessageDto> messages = getConversationMessages(conversation.getId(), user);
                    dto.setMessages(messages);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DirectMessageConversation getOrCreateConversation(User user1, User user2) {
        return conversationRepository.findConversationBetweenUsers(user1, user2)
                .orElseGet(() -> {
                    DirectMessageConversation conversation = DirectMessageConversation.builder()
                            .user1(user1)
                            .user2(user2)
                            .build();
                    return conversationRepository.save(conversation);
                });
    }

    @Override
    public DirectMessageConversation getConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
    }

    @Override
    public List<DirectMessageDto> getConversationMessages(Long conversationId, User currentUser) {
        DirectMessageConversation conversation = getConversationById(conversationId);
        
        // Security check - only participants can view messages
        if (!conversation.involvesUser(currentUser)) {
            throw new IllegalStateException("You are not authorized to view messages in this conversation");
        }
        
        List<DirectMessage> messages = messageRepository.findByConversationOrderBySentAtAsc(conversation);
        return messages.stream()
                .map(message -> DirectMessageDto.fromEntity(message, currentUser.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DirectMessageDto sendMessage(Long conversationId, User sender, String content) {
        DirectMessageConversation conversation = getConversationById(conversationId);
        
        // Security check - only participants can send messages
        if (!conversation.involvesUser(sender)) {
            throw new IllegalStateException("You are not authorized to send messages in this conversation");
        }
        
        // Check if users are friends
        if (!canSendMessage(sender, conversation.getOtherUser(sender))) {
            throw new IllegalStateException("You can only send messages to friends");
        }
        
        DirectMessage message = DirectMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .isRead(false)
                .build();
        
        message = messageRepository.save(message);
        
        // Update conversation read status and last update time
        boolean isSenderUser1 = conversation.getUser1().getId().equals(sender.getId());
        if (isSenderUser1) {
            conversation.setUser1Read(true);
            conversation.setUser2Read(false);
        } else {
            conversation.setUser1Read(false);
            conversation.setUser2Read(true);
        }
        
        conversationRepository.save(conversation);
        
        // Send notification to the other participant
        User recipient = conversation.getOtherUser(sender);
        String senderName = sender.getFirstName() + " " + sender.getLastName();
        systemMessageService.sendNewDirectMessageNotification(recipient, senderName, conversation.getId());
        
        return DirectMessageDto.fromEntity(message, sender.getId());
    }

    @Override
    public DirectMessageDto getMessageById(Long messageId, User currentUser) {
        DirectMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        
        // Security check - only participants can view messages
        if (!message.getConversation().involvesUser(currentUser)) {
            throw new IllegalStateException("You are not authorized to view this message");
        }
        
        return DirectMessageDto.fromEntity(message, currentUser.getId());
    }

    @Override
    @Transactional
    public void markConversationAsRead(Long conversationId, User currentUser) {
        DirectMessageConversation conversation = getConversationById(conversationId);
        
        // Security check - only participants can mark as read
        if (!conversation.involvesUser(currentUser)) {
            throw new IllegalStateException("You are not authorized to mark this conversation as read");
        }
        
        // Mark messages as read
        messageRepository.markMessagesAsReadInConversation(conversation, currentUser.getId());
        
        // Update conversation read status
        boolean isUser1 = conversation.getUser1().getId().equals(currentUser.getId());
        if (isUser1) {
            conversation.setUser1Read(true);
        } else {
            conversation.setUser2Read(true);
        }
        
        conversationRepository.save(conversation);
    }

    @Override
    public long countUnreadMessages(User user) {
        List<DirectMessageConversation> conversations = conversationRepository.findConversationsByUser(user);
        return conversations.stream()
                .mapToLong(conversation -> messageRepository.countUnreadMessagesInConversation(conversation, user.getId()))
                .sum();
    }

    @Override
    public boolean canSendMessage(User sender, User recipient) {
        // Check if users are friends (bidirectional friendship)
        return friendRepository.existsByUserAndFriendUser(sender, recipient) ||
               friendRepository.existsByUserAndFriendUser(recipient, sender);
    }
} 