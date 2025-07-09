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
    
    public enum OfferStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        COUNTERED
    }
    
    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
    
    public boolean isOffer() {
        return offerAmount != null;
    }
} 