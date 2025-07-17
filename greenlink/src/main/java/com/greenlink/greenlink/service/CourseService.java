package com.greenlink.greenlink.service;

import com.greenlink.greenlink.dto.CourseDto;
import com.greenlink.greenlink.model.Course;
import com.greenlink.greenlink.model.Quiz;
import com.greenlink.greenlink.model.QuizAnswer;
import com.greenlink.greenlink.model.QuizResult;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.CourseRepository;
import com.greenlink.greenlink.repository.QuizRepository;
import com.greenlink.greenlink.repository.QuizResultRepository;
import com.greenlink.greenlink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private QuizResultRepository quizResultRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private QuizRepository quizRepository;

    // Preia toate cursurile
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(course -> new CourseDto(
                        course.getId(),
                        course.getTitle(),
                        course.getDescription(),
                        course.getDuration()
                ))
                .collect(Collectors.toList());
    }

    // Preia un curs după ID
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cursul nu a fost găsit"));
        return new CourseDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getDuration()
        );
    }

    // Adaugă un curs nou
    public CourseDto addCourse(CourseDto courseDto) {
        Course course = new Course(
                null,
                courseDto.getTitle(),
                courseDto.getDescription(),
                courseDto.getDuration()
        );
        Course savedCourse = courseRepository.save(course);
        return new CourseDto(
                savedCourse.getId(),
                savedCourse.getTitle(),
                savedCourse.getDescription(),
                savedCourse.getDuration()
        );
    }
    
    // Salvează rezultatul unui quiz
    @Transactional
    public QuizResult saveQuizResult(Long userId, Long courseId, int correctAnswers, int totalQuestions, int pointsEarned) {
        return saveQuizResult(userId, courseId, correctAnswers, totalQuestions, pointsEarned, null, null);
    }
    
    @Transactional
    public QuizResult saveQuizResult(Long userId, Long courseId, int correctAnswers, int totalQuestions, 
                                    int pointsEarned, String reflectionText, Map<String, Object> answersData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit"));
        
        // Create a new quiz result
        QuizResult quizResult = new QuizResult();
        quizResult.setUser(user);
        
        // For course quizzes, we'll use a placeholder quiz if needed
        try {
            // Try to find existing quiz or create a placeholder
            Quiz quiz = quizRepository.findById(courseId).orElse(null);
            if (quiz == null) {
                // Create a temporary quiz object just to satisfy the database constraint
                quiz = new Quiz();
                quiz.setId(courseId);
                quiz.setTitle("Course " + courseId);
                quiz.setDescription("Auto-created placeholder");
                quiz.setPointsValue(pointsEarned);
                quiz = quizRepository.save(quiz);
            }
            quizResult.setQuiz(quiz);
        } catch (Exception e) {
            // If quiz creation fails, log error but continue
            System.err.println("Error creating quiz placeholder: " + e.getMessage());
            // This will fail due to NOT NULL constraint, but attempt is made
        }
        
        quizResult.setScore(correctAnswers);
        quizResult.setPointsEarned(pointsEarned);
        quizResult.setCompletedAt(LocalDateTime.now());
        
        // Save reflection text if provided
        if (reflectionText != null && !reflectionText.isEmpty()) {
            quizResult.setReflectionText(reflectionText);
        }
        
        // Save the quiz result
        QuizResult savedResult = quizResultRepository.save(quizResult);
        
        // Save answers if provided
        if (answersData != null && !answersData.isEmpty()) {
            try {
                // Iterate through answers data entries
                for (Map.Entry<String, Object> entry : answersData.entrySet()) {
                    String questionKey = entry.getKey();
                    // Try to extract question number from key (e.g., "question_1" -> 1)
                    int questionNumber = 0;
                    if (questionKey.startsWith("question_")) {
                        try {
                            questionNumber = Integer.parseInt(questionKey.substring("question_".length()));
                        } catch (NumberFormatException e) {
                            // Skip entries with invalid question numbers
                            continue;
                        }
                    } else {
                        // Skip entries not matching our expected format
                        continue;
                    }
                    
                    // Get the answer data which is stored as a Map
                    @SuppressWarnings("unchecked")
                    Map<String, Object> answerData = (Map<String, Object>) entry.getValue();
                    if (answerData == null) continue;
                    
                    // Extract answer data
                    String questionText = "Question " + questionNumber;
                    String userAnswer = answerData.containsKey("selectedOption") ? 
                                        answerData.get("selectedOption").toString() : "";
                    boolean isCorrect = answerData.containsKey("isCorrect") ? 
                                        Boolean.parseBoolean(answerData.get("isCorrect").toString()) : false;
                    
                    // Create and save the answer
                    QuizAnswer answer = new QuizAnswer(savedResult, questionNumber, questionText, userAnswer, isCorrect);
                    savedResult.addAnswer(answer);
                }
                // The cascade will save all answers
                savedResult = quizResultRepository.save(savedResult);
            } catch (Exception e) {
                // Log error but continue - we still want to save the result even if answers fail
                System.err.println("Error saving quiz answers: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return savedResult;
    }
}
