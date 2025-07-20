package com.greenlink.greenlink.dto;

import com.greenlink.greenlink.model.DirectMessageConversation;
import com.greenlink.greenlink.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class DirectMessageConversationDto {
    private Long id;
    private Long otherUserId;
    private String otherUserName;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DirectMessageDto> messages;
    
    public DirectMessageConversationDto() {}
    
    public DirectMessageConversationDto(Long id, Long otherUserId, String otherUserName, 
                                       String lastMessageContent, LocalDateTime lastMessageTime, 
                                       boolean isRead, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.lastMessageContent = lastMessageContent;
        this.lastMessageTime = lastMessageTime;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public static DirectMessageConversationDto fromEntity(DirectMessageConversation conversation, User currentUser) {
        User otherUser = conversation.getOtherUser(currentUser);
        boolean isRead = conversation.getUser1().getId().equals(currentUser.getId()) ? 
                        conversation.isUser1Read() : conversation.isUser2Read();
        
        return new DirectMessageConversationDto(
            conversation.getId(),
            otherUser.getId(),
            otherUser.getFirstName() + " " + otherUser.getLastName(),
            conversation.getMessages().isEmpty() ? null : 
                conversation.getMessages().get(conversation.getMessages().size() - 1).getContent(),
            conversation.getMessages().isEmpty() ? null : 
                conversation.getMessages().get(conversation.getMessages().size() - 1).getSentAt(),
            isRead,
            conversation.getCreatedAt(),
            conversation.getUpdatedAt()
        );
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getOtherUserId() {
        return otherUserId;
    }
    
    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }
    
    public String getOtherUserName() {
        return otherUserName;
    }
    
    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }
    
    public String getLastMessageContent() {
        return lastMessageContent;
    }
    
    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }
    
    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }
    
    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<DirectMessageDto> getMessages() {
        return messages;
    }
    
    public void setMessages(List<DirectMessageDto> messages) {
        this.messages = messages;
    }
} 