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
    private ChallengeType type;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
    private int progressPercentage;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public enum ChallengeType {
        DEFAULT, AMBASADOOR, MAESTER, SHELF_WHISPERER, CART_GOBLIN
    }

    public enum ChallengeStatus {
        ACTIVE, IN_PROGRESS, COMPLETED, EXPIRED
    }

    // Constructors
    public Challenge() {}

    public Challenge(String title, String description, int points, ChallengeType type) {
        this.title = title;
        this.description = description;
        this.points = points;
        this.type = type;
        this.status = ChallengeStatus.ACTIVE;
        this.progressPercentage = 0;
        this.startDate = LocalDateTime.now();
        this.endDate = calculateEndDate();
        this.updatedAt = LocalDateTime.now();
    }

    private LocalDateTime calculateEndDate() {
        // All challenges are unlimited duration
        return null;
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

    public ChallengeType getType() {
        return type;
    }

    public void setType(ChallengeType type) {
        this.type = type;
    }

    public ChallengeStatus getStatus() {
        return status;
    }

    public void setStatus(ChallengeStatus status) {
        this.status = status;
        if (status == ChallengeStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = Math.min(100, Math.max(0, progressPercentage));
        this.updatedAt = LocalDateTime.now();
    }

    public void setProgress(int progress) {
        setProgressPercentage(progress);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}