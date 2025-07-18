package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.*;
import com.greenlink.greenlink.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PointsService pointsService;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    // Metode pentru Quiz
    public Quiz createQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz-ul nu a fost găsit"));
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    // Metode pentru Question
    public Question addQuestionToQuiz(Long quizId, Question question) {
        Quiz quiz = getQuizById(quizId);
        question.setQuiz(quiz);
        return questionRepository.save(question);
    }

    public List<Question> getQuizQuestions(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }

    // Metode pentru QuizResult
    @Transactional
    public QuizResult submitQuiz(Long quizId, Long userId, Map<Long, String> answers) {
        Quiz quiz = getQuizById(quizId);
        User user = userService.getUserById(userId);

        // Verifică dacă utilizatorul a mai completat acest quiz
        if (quizResultRepository.existsByUserIdAndQuizId(userId, quizId)) {
            throw new RuntimeException("Ai completat deja acest quiz");
        }

        // Calculează scorul
        int score = calculateScore(quizId, answers);
        int maxScore = calculateMaxScore(quizId);
        int pointsEarned = calculatePointsEarned(score, maxScore, quiz.getPointsValue());

        // Creează rezultatul
        QuizResult result = new QuizResult();
        result.setUser(user);
        result.setQuiz(quiz);
        result.setScore(score);
        result.setPointsEarned(pointsEarned);
        result.setCompletedAt(LocalDateTime.now());

        // Salvează rezultatul și actualizează punctele utilizatorului
        QuizResult savedResult = quizResultRepository.save(result);
        pointsService.awardQuizPoints(userId, quizId, quiz.getTitle(), pointsEarned);

        
        // Save each answer
        for(Map.Entry<Long, String> entry : answers.entrySet()) {
            Long questionId = entry.getKey();
            String userAnswer = entry.getValue();
            
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Intrebarea nu a fost gasita"));
            
            boolean isCorrect = isCorrectAnswer(question, userAnswer);
            
            QuizAnswer quizAnswer = new QuizAnswer(savedResult, questionId.intValue(), question.getQuestionText(), userAnswer, isCorrect);
            quizAnswerRepository.save(quizAnswer);
        }
        return savedResult;
    }

    // Metode pentru statistici și rezultate
    public List<QuizResult> getUserQuizResults(Long userId) {
        return quizResultRepository.findByUserId(userId);
    }

    public Page<QuizResult> getUserQuizResultsPaginated(Long userId, Pageable pageable) {
        return quizResultRepository.findByUserId(userId, pageable);
    }

    public Double getQuizAverageScore(Long quizId) {
        return quizResultRepository.calculateAverageScoreForQuiz(quizId);
    }

    public List<QuizResult> getTopScores(Long quizId, int limit) {
        return quizResultRepository.findTopScoresForQuiz(quizId,
                Pageable.ofSize(limit));
    }

    // Metode helper private
    private int calculateScore(Long quizId, Map<Long, String> answers) {
        List<Question> questions = questionRepository.findByQuizId(quizId);
        return questions.stream()
                .mapToInt(question -> {
                    String userAnswer = answers.get(question.getId());
                    return isCorrectAnswer(question, userAnswer) ? question.getPoints() : 0;
                })
                .sum();
    }

    private boolean isCorrectAnswer(Question question, String userAnswer) {
        if (userAnswer == null) return false;
        return question.getCorrectAnswer().equalsIgnoreCase(userAnswer.trim());
    }

    private int calculateMaxScore(Long quizId) {
        return questionRepository.findByQuizId(quizId).stream()
                .mapToInt(Question::getPoints)
                .sum();
    }

    private int calculatePointsEarned(int score, int maxScore, int quizPoints) {
        // Calculează procentajul și acordă puncte proporțional
        double percentage = (double) score / maxScore;
        return (int) Math.round(percentage * quizPoints);
    }
}