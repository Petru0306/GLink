package com.greenlink.greenlink.dto;

import com.greenlink.greenlink.model.DirectMessage;
import com.greenlink.greenlink.model.User;

import java.time.LocalDateTime;

public class DirectMessageDto {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
    private boolean isRead;
    private boolean isCurrentUserSender;
    private String tempId; // For tracking temporary messages on frontend
    
    public DirectMessageDto() {}
    
    public DirectMessageDto(Long id, Long conversationId, Long senderId, String senderName, 
                           String content, LocalDateTime sentAt, boolean isRead, boolean isCurrentUserSender) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.sentAt = sentAt;
        this.isRead = isRead;
        this.isCurrentUserSender = isCurrentUserSender;
    }
    
    public static DirectMessageDto fromEntity(DirectMessage message, Long currentUserId) {
        return new DirectMessageDto(
            message.getId(),
            message.getConversation().getId(),
            message.getSender().getId(),
            message.getSender().getFirstName() + " " + message.getSender().getLastName(),
            message.getContent(),
            message.getSentAt(),
            message.isRead(),
            message.getSender().getId().equals(currentUserId)
        );
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
    
    public Long getSenderId() {
        return senderId;
    }
    
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public boolean isCurrentUserSender() {
        return isCurrentUserSender;
    }
    
    public void setCurrentUserSender(boolean currentUserSender) {
        isCurrentUserSender = currentUserSender;
    }
    
    public String getTempId() {
        return tempId;
    }
    
    public void setTempId(String tempId) {
        this.tempId = tempId;
    }
} 