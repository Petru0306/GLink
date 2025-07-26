package com.greenlink.greenlink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
    private boolean isCurrentUserSender;
    private boolean isOffer;
    private Double offerAmount;
    private String offerStatus;
    private Long messageId;
    private String tempId; // Temporary ID for client-side tracking
    
    // Enhanced offer fields
    private Long version; // For optimistic locking
    private LocalDateTime offerExpiresAt;
    private Long counterOfferMessageId;
    private Long originalOfferMessageId;
    private boolean isExpired;
    private boolean canBeActedUpon;
    private boolean isCounterOffer;
    private boolean isOriginalOffer;
    
    // Event tracking
    private String eventType; // CREATED, UPDATED, COUNTERED, etc.
    private Long timestamp;
    
    public static ChatMessageDto fromMessageDto(MessageDto messageDto) {
        return ChatMessageDto.builder()
                .messageId(messageDto.getId())
                .conversationId(messageDto.getConversationId())
                .senderId(messageDto.getSenderId())
                .senderName(messageDto.getSenderName())
                .content(messageDto.getContent())
                .sentAt(messageDto.getSentAt())
                .isCurrentUserSender(messageDto.isCurrentUserSender())
                .isOffer(messageDto.isOffer())
                .offerAmount(messageDto.getOfferAmount())
                .offerStatus(messageDto.getOfferStatus())
                .version(messageDto.getVersion())
                .offerExpiresAt(messageDto.getOfferExpiresAt())
                .counterOfferMessageId(messageDto.getCounterOfferMessageId())
                .originalOfferMessageId(messageDto.getOriginalOfferMessageId())
                .isExpired(messageDto.isExpired())
                .canBeActedUpon(messageDto.isCanBeActedUpon())
                .isCounterOffer(messageDto.isCounterOffer())
                .isOriginalOffer(messageDto.isOriginalOffer())
                .timestamp(System.currentTimeMillis())
                .build();
    }
} 