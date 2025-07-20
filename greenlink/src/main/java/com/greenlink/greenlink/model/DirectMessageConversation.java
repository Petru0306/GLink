package com.greenlink.greenlink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "direct_message_conversations")
public class DirectMessageConversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;
    
    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_user1_read", nullable = false)
    @Builder.Default
    private boolean isUser1Read = false;
    
    @Column(name = "is_user2_read", nullable = false)
    @Builder.Default
    private boolean isUser2Read = false;
    
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DirectMessage> messages = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get the other user in the conversation
     */
    public User getOtherUser(User currentUser) {
        if (user1.getId().equals(currentUser.getId())) {
            return user2;
        } else if (user2.getId().equals(currentUser.getId())) {
            return user1;
        }
        throw new IllegalArgumentException("User is not part of this conversation");
    }
    
    /**
     * Check if a user is part of this conversation
     */
    public boolean involvesUser(User user) {
        return user1.getId().equals(user.getId()) || user2.getId().equals(user.getId());
    }
} 