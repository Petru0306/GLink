package com.greenlink.greenlink.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_answers")
public class QuizAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_result_id", nullable = false)
    private QuizResult quizResult;

    @Column(name = "question_number", nullable = false)
    private int questionNumber;

    @Column(name = "question_text", nullable = false, length = 1000)
    private String questionText;

    @Column(name = "user_answer", nullable = false, length = 500)
    private String userAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public QuizAnswer() {
        this.createdAt = LocalDateTime.now();
    }

    public QuizAnswer(QuizResult quizResult, int questionNumber, String questionText, String userAnswer, boolean correct) {
        this.quizResult = quizResult;
        this.questionNumber = questionNumber;
        this.questionText = questionText;
        this.userAnswer = userAnswer;
        this.correct = correct;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuizResult getQuizResult() {
        return quizResult;
    }

    public void setQuizResult(QuizResult quizResult) {
        this.quizResult = quizResult;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
