package com.greenlink.greenlink.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_challenges")
public class UserChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    private int progressPercentage;
    
    @Column(name = "start_date")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private Challenge.ChallengeStatus status;

    // Constructors
    public UserChallenge() {}

    public UserChallenge(User user, Challenge challenge) {
        this.user = user;
        this.challenge = challenge;
        this.progressPercentage = 0;
        this.startedAt = LocalDateTime.now();
        this.status = Challenge.ChallengeStatus.ACTIVE;
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

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = Math.min(100, Math.max(0, progressPercentage));
        if (this.progressPercentage == 100 && this.status != Challenge.ChallengeStatus.COMPLETED) {
            this.status = Challenge.ChallengeStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Challenge.ChallengeStatus getStatus() {
        return status;
    }

    public void setStatus(Challenge.ChallengeStatus status) {
        this.status = status;
        if (status == Challenge.ChallengeStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }
} 