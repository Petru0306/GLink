package com.greenlink.greenlink.dto;

import com.greenlink.greenlink.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
    private boolean isRead;
    private boolean isCurrentUserSender;
    private boolean isOffer;
    private Double offerAmount;
    private String offerStatus;
    
    public static MessageDto fromEntity(Message message, Long currentUserId) {
        boolean isCurrentUserSender = message.getSender().getId().equals(currentUserId);
        
        return MessageDto.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFirstName() + " " + message.getSender().getLastName())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .isRead(message.isRead())
                .isCurrentUserSender(isCurrentUserSender)
                .isOffer(message.isOffer())
                .offerAmount(message.getOfferAmount())
                .offerStatus(message.getOfferStatus() != null ? message.getOfferStatus().name() : null)
                .build();
    }
} 