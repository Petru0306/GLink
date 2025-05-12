package com.greenlink.greenlink.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private String questionText;

    @ElementCollection
    @CollectionTable(name = "question_options",
            joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text")
    private List<String> options;

    @Column(nullable = false)
    private String correctAnswer;

    @Column
    private String explanation;

    @Column(nullable = false)
    private int points = 1;

    @Enumerated(EnumType.STRING)
    private QuestionType type = QuestionType.SINGLE_CHOICE;

    public enum QuestionType {
        SINGLE_CHOICE,
        MULTIPLE_CHOICE,
        TRUE_FALSE,
        TEXT_ANSWER
    }

    // Constructors
    public Question() {
    }

    public Question(Quiz quiz, String questionText, List<String> options,
                    String correctAnswer, QuestionType type) {
        this.quiz = quiz;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.type = type;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }
}