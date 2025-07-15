package com.greenlink.greenlink.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "challenges")
public class Challenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int points;

    @Enumerated(EnumType.STRING)
    private ChallengeCategory category;

    @Column(nullable = false)
    private String badgeName;

    @Column(nullable = false)
    private String emoji;

    @Column(nullable = false)
    private int targetValue;

    @Column(nullable = false)
    private String progressEvent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ChallengeCategory {
        DEFAULT("Default Challenges", "🌱", "Basic onboarding challenges"),
        AMBASSADOR("Ambassador", "🤝", "Friends-based challenges"),
        MAESTER("Maester", "📚", "Lesson-based challenges"),
        SHELF_WHISPERER("Shelf Whisperer", "🏪", "Seller challenges"),
        CART_GOBLIN("Cart Goblin", "🛒", "Buyer challenges");

        private final String displayName;
        private final String icon;
        private final String description;

        ChallengeCategory(String displayName, String icon, String description) {
            this.displayName = displayName;
            this.icon = icon;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getIcon() {
            return icon;
        }

        public String getDescription() {
            return description;
        }
    }

    // Constructors
    public Challenge() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Challenge(String title, String description, int points, ChallengeCategory category, 
                    String badgeName, String emoji, int targetValue, String progressEvent) {
        this.title = title;
        this.description = description;
        this.points = points;
        this.category = category;
        this.badgeName = badgeName;
        this.emoji = emoji;
        this.targetValue = targetValue;
        this.progressEvent = progressEvent;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public ChallengeCategory getCategory() {
        return category;
    }

    public void setCategory(ChallengeCategory category) {
        this.category = category;
    }

    public String getBadgeName() {
        return badgeName;
    }

    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(int targetValue) {
        this.targetValue = targetValue;
    }

    public String getProgressEvent() {
        return progressEvent;
    }

    public void setProgressEvent(String progressEvent) {
        this.progressEvent = progressEvent;
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
}