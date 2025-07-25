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
public class OfferEvent {
    public enum EventType {
        OFFER_CREATED,
        OFFER_UPDATED,
        OFFER_ACCEPTED,
        OFFER_REJECTED,
        COUNTER_OFFER_CREATED,
        OFFER_EXPIRED,
        OFFER_CANCELLED,
        PRICE_UPDATED
    }
    
    private EventType eventType;
    private Long messageId;
    private Long conversationId;
    private String offerStatus;
    private Double offerAmount;
    private Long senderId;
    private String senderName;
    private Long responderId;
    private String responderName;
    private LocalDateTime timestamp;
    private Long version;
    private LocalDateTime offerExpiresAt;
    private Long counterOfferMessageId;
    private Long originalOfferMessageId;
    private boolean isExpired;
    private boolean canBeActedUpon;
    private boolean isCounterOffer;
    private boolean isOriginalOffer;
    
    // Additional context for specific events
    private String reason; // For rejections, cancellations, etc.
    private Double originalPrice;
    private Double negotiatedPrice;
} 