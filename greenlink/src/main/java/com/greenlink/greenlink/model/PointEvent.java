package com.greenlink.greenlink.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_events")
public class PointEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false)
    private String eventType;

    @Column(length = 500)
    private String description;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "related_entity_type")
    private String relatedEntityType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "old_level")
    private int oldLevel;

    @Column(name = "new_level")
    private int newLevel;

    @Column(name = "leveled_up")
    private boolean leveledUp;

    // Constructors
    public PointEvent() {
        this.createdAt = LocalDateTime.now();
    }

    public PointEvent(User user, int points, String eventType, String description) {
        this();
        this.user = user;
        this.points = points;
        this.eventType = eventType;
        this.description = description;
    }

    public PointEvent(User user, int points, String eventType, String description, 
                     Long relatedEntityId, String relatedEntityType) {
        this(user, points, eventType, description);
        this.relatedEntityId = relatedEntityId;
        this.relatedEntityType = relatedEntityType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public void setOldLevel(int oldLevel) {
        this.oldLevel = oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public void setNewLevel(int newLevel) {
        this.newLevel = newLevel;
    }

    public boolean isLeveledUp() {
        return leveledUp;
    }

    public void setLeveledUp(boolean leveledUp) {
        this.leveledUp = leveledUp;
    }

    // Event type constants
    public static final class EventType {
        public static final String CHALLENGE_COMPLETED = "CHALLENGE_COMPLETED";
        public static final String QUIZ_COMPLETED = "QUIZ_COMPLETED";
        public static final String MARKETPLACE_SALE = "MARKETPLACE_SALE";
        public static final String MARKETPLACE_PURCHASE = "MARKETPLACE_PURCHASE";
        public static final String LESSON_COMPLETED = "LESSON_COMPLETED";
        public static final String CARBON_CALCULATOR_USED = "CARBON_CALCULATOR_USED";
        public static final String RECYCLING_MAP_EXPLORED = "RECYCLING_MAP_EXPLORED";
        public static final String PROFILE_UPDATED = "PROFILE_UPDATED";
        public static final String FRIEND_ADDED = "FRIEND_ADDED";
        public static final String MESSAGE_SENT = "MESSAGE_SENT";
        public static final String OFFER_MADE = "OFFER_MADE";
        public static final String OFFER_ACCEPTED = "OFFER_ACCEPTED";
    }
} 