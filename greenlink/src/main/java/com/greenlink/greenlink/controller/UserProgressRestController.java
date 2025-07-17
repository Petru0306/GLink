package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.QuizResult;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.QuizResultRepository;
import com.greenlink.greenlink.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-progress")
public class UserProgressRestController {

    @Autowired
    private QuizResultRepository quizResultRepository;
    
    @Autowired
    private CourseService courseService;
    
    /**
     * Get all completed lessons for the authenticated user
     */
    @GetMapping
    public ResponseEntity<?> getUserProgress(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.ok(response);
        }
        
        // Find all quiz results for the user
        List<QuizResult> userResults = quizResultRepository.findByUserId(currentUser.getId());
        
        // Map to quiz/lesson IDs
        List<Long> completedLessons = userResults.stream()
                .map(result -> result.getQuiz() != null ? result.getQuiz().getId() : 0L)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        response.put("userId", currentUser.getId());
        response.put("completedLessons", completedLessons);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if a specific lesson is completed
     */
    @GetMapping(params = "lessonId")
    public ResponseEntity<?> isLessonCompleted(
            @RequestParam Long lessonId,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (currentUser == null) {
            response.put("authenticated", false);
            response.put("completed", false);
            return ResponseEntity.ok(response);
        }
        
        boolean completed = quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), lessonId);
        
        response.put("authenticated", true);
        response.put("userId", currentUser.getId());
        response.put("lessonId", lessonId);
        response.put("completed", completed);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Save lesson completion with answers
     */
    @PostMapping("/complete-lesson/{lessonId}")
    public ResponseEntity<?> completeLessonWithAnswers(
            @PathVariable Long lessonId,
            @RequestBody Map<String, Object> completionData,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check authentication
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Check if already completed
        if (quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), lessonId)) {
            response.put("success", false);
            response.put("message", "Lesson already completed");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Extract data from request
            int correctAnswers = Integer.parseInt(completionData.getOrDefault("correctAnswers", "0").toString());
            int totalQuestions = Integer.parseInt(completionData.getOrDefault("totalQuestions", "5").toString());
            int pointsEarned = Integer.parseInt(completionData.getOrDefault("pointsEarned", "0").toString());
            String reflectionText = (String) completionData.getOrDefault("reflectionText", "");
            
            // Get answers data if available
            @SuppressWarnings("unchecked")
            Map<String, Object> answersData = (Map<String, Object>) completionData.getOrDefault("answers", new HashMap<>());
            
            // Save the quiz result with all data
            courseService.saveQuizResult(
                currentUser.getId(), lessonId, correctAnswers, totalQuestions, pointsEarned, reflectionText, answersData
            );
            
            response.put("success", true);
            response.put("message", "Lesson completed successfully");
            response.put("lessonId", lessonId);
            response.put("pointsEarned", pointsEarned);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error completing lesson: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
