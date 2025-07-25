package com.greenlink.greenlink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "messages")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;
    
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private LocalDateTime sentAt;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;
    
    @Column(nullable = true)
    private Double offerAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private OfferStatus offerStatus;
    
    // Add optimistic locking to prevent race conditions
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
    
    // Add offer expiration tracking
    @Column(nullable = true)
    private LocalDateTime offerExpiresAt;
    
    // Add reference to counter-offer for tracking offer chains
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counter_offer_message_id", nullable = true)
    private Message counterOfferMessage;
    
    // Add original offer reference for counter-offers
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_offer_message_id", nullable = true)
    private Message originalOfferMessage;
    
    public enum OfferStatus {
        PENDING,           // Initial state - sender view
        ACTION_REQUIRED,   // Receiver view with action buttons
        ACCEPTED,         // Offer accepted
        REJECTED,         // Offer rejected
        COUNTERED,        // Counter-offer made
        EXPIRED,          // Offer expired
        CANCELLED         // Offer cancelled by sender
    }
    
    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
        // Set offer expiration to 7 days from creation if it's an offer
        if (this.isOffer() && this.offerExpiresAt == null) {
            this.offerExpiresAt = LocalDateTime.now().plusDays(7);
        }
    }
    
    public boolean isOffer() {
        return offerAmount != null;
    }
    
    public boolean isExpired() {
        return offerExpiresAt != null && LocalDateTime.now().isAfter(offerExpiresAt);
    }
    
    public boolean canBeActedUpon() {
        return isOffer() && 
               (offerStatus == OfferStatus.PENDING || offerStatus == OfferStatus.ACTION_REQUIRED) &&
               !isExpired();
    }
    
    public boolean isCounterOffer() {
        return originalOfferMessage != null;
    }
    
    public boolean isOriginalOffer() {
        return counterOfferMessage != null;
    }
} 