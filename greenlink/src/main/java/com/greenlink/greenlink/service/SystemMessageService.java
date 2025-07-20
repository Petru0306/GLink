package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.MessageType;
import com.greenlink.greenlink.model.SystemMessage;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.SystemMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemMessageService {

    @Autowired
    private SystemMessageRepository systemMessageRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public SystemMessage sendSystemMessage(User recipient, String content, String type) {
        MessageType messageType;
        try {
            messageType = MessageType.valueOf(type);
        } catch (IllegalArgumentException e) {
            messageType = MessageType.SYSTEM;
        }

        SystemMessage message = new SystemMessage(recipient, content, messageType);
        SystemMessage savedMessage = systemMessageRepository.save(message);
        
        // Send real-time notification via WebSocket
        sendNotificationToUser(recipient.getId(), savedMessage);
        
        return savedMessage;
    }
    
    /**
     * Send real-time notification to a specific user via WebSocket
     */
    private void sendNotificationToUser(Long userId, SystemMessage message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "SYSTEM_NOTIFICATION");
            notification.put("messageId", message.getId());
            notification.put("content", message.getContent());
            notification.put("messageType", message.getType().toString());
            notification.put("createdAt", message.getCreatedAt());
            notification.put("read", message.isRead());
            
            messagingTemplate.convertAndSendToUser(
                userId.toString(), 
                "/queue/notifications", 
                notification
            );
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
        }
    }
    
    /**
     * Send real-time notification for friend requests
     */
    public void sendFriendRequestNotification(User recipient, User sender) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "FRIEND_REQUEST");
            notification.put("senderId", sender.getId());
            notification.put("senderName", sender.getFirstName() + " " + sender.getLastName());
            notification.put("senderEmail", sender.getEmail());
            notification.put("content", sender.getFirstName() + " " + sender.getLastName() + " wants to be your friend!");
            notification.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSendToUser(
                recipient.getId().toString(), 
                "/queue/notifications", 
                notification
            );
        } catch (Exception e) {
            System.err.println("Failed to send friend request notification: " + e.getMessage());
        }
    }
    
    /**
     * Send real-time notification for new conversations
     */
    public void sendNewConversationNotification(User recipient, String senderName, Long conversationId) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NEW_CONVERSATION");
            notification.put("conversationId", conversationId);
            notification.put("senderName", senderName);
            notification.put("content", "New message from " + senderName);
            notification.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSendToUser(
                recipient.getId().toString(), 
                "/queue/notifications", 
                notification
            );
        } catch (Exception e) {
            System.err.println("Failed to send conversation notification: " + e.getMessage());
        }
    }
    
    /**
     * Send real-time notification for new direct messages
     */
    public void sendNewDirectMessageNotification(User recipient, String senderName, Long conversationId) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NEW_DIRECT_MESSAGE");
            notification.put("conversationId", conversationId);
            notification.put("senderName", senderName);
            notification.put("content", "New direct message from " + senderName);
            notification.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSendToUser(
                recipient.getId().toString(), 
                "/queue/notifications", 
                notification
            );
        } catch (Exception e) {
            System.err.println("Failed to send direct message notification: " + e.getMessage());
        }
    }

    @Transactional
    public void markAsRead(Long messageId) {
        systemMessageRepository.findById(messageId).ifPresent(message -> {
            message.setRead(true);
            systemMessageRepository.save(message);
        });
    }

    @Transactional
    public void markAllAsRead(User user) {
        systemMessageRepository.findByRecipientAndReadOrderByCreatedAtDesc(user, false)
            .forEach(message -> {
                message.setRead(true);
                systemMessageRepository.save(message);
            });
    }
    
    public List<SystemMessage> getUnreadMessages(User user) {
        return systemMessageRepository.findByRecipientAndReadOrderByCreatedAtDesc(user, false);
    }
    
    public long countUnreadMessages(User user) {
        return systemMessageRepository.countByRecipientAndRead(user, false);
    }
}
