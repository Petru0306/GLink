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
                .build();
    }
} 