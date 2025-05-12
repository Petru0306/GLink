package com.greenlink.greenlink.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int pointsValue;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<QuizResult> results;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<Question> questions;

    // Constructors
    public Quiz() {}

    public Quiz(String title, String description, int pointsValue) {
        this.title = title;
        this.description = description;
        this.pointsValue = pointsValue;
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

    public int getPointsValue() {
        return pointsValue;
    }

    public void setPointsValue(int pointsValue) {
        this.pointsValue = pointsValue;
    }

    public List<QuizResult> getResults() {
        return results;
    }

    public void setResults(List<QuizResult> results) {
        this.results = results;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}