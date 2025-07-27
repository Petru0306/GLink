package com.greenlink.greenlink.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_results")
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = true)
    private Quiz quiz;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private int pointsEarned;
    
    // Reflection text from the user
    @Column(name = "reflection_text", length = 1000)
    private String reflectionText;
    
    // User's uploaded final task image URL
    @Column(name = "user_image_url", length = 500)
    private String userImageUrl;
    
    // Reference to answers
    @OneToMany(mappedBy = "quizResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizAnswer> answers = new ArrayList<>();

    // Constructors
    public QuizResult() {
        this.completedAt = LocalDateTime.now();
    }

    public QuizResult(User user, Quiz quiz) {
        this.user = user;
        this.quiz = quiz;
        this.completedAt = LocalDateTime.now();
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

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }
    
    public String getReflectionText() {
        return reflectionText;
    }
    
    public void setReflectionText(String reflectionText) {
        this.reflectionText = reflectionText;
    }
    
    public String getUserImageUrl() {
        return userImageUrl;
    }
    
    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }
    
    public List<QuizAnswer> getAnswers() {
        return answers;
    }
    
    public void setAnswers(List<QuizAnswer> answers) {
        this.answers = answers;
    }
    
    // Helper method to add an answer
    public void addAnswer(QuizAnswer answer) {
        answers.add(answer);
        answer.setQuizResult(this);
    }
}