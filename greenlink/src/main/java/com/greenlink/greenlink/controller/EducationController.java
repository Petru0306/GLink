package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.CourseDto;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.QuizResultRepository;
import com.greenlink.greenlink.service.CourseService;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ChallengeTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/educatie")
public class EducationController {

    @Autowired
    private CourseService courseService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private ChallengeTrackingService challengeTrackingService;

    @GetMapping
    public String getAllCourses(Model model, @AuthenticationPrincipal User currentUser) {
        List<CourseDto> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        
        // Add authentication info to model
        model.addAttribute("isAuthenticated", currentUser != null);
        
        // Add completed lessons for authenticated users
        if (currentUser != null) {
            List<Long> completedLessons = new ArrayList<>();
            for (long i = 1; i <= 6; i++) {
                if (quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), i)) {
                    completedLessons.add(i);
                }
            }
            // Convert to JSON string for easier JavaScript parsing
            ObjectMapper mapper = new ObjectMapper();
            try {
                String completedLessonsJson = mapper.writeValueAsString(completedLessons);
                model.addAttribute("completedLessons", completedLessonsJson);
            } catch (Exception e) {
                model.addAttribute("completedLessons", "[]");
            }
        }
        
        return "educatie"; // Returnează fișierul education.html
    }

    @GetMapping("/curs/{courseId}")
    public String getCourseDetails(@PathVariable Long courseId, Model model, @AuthenticationPrincipal User currentUser) {
        // For lessons 4, 5, 6, create mock course data since they don't exist in database
        CourseDto course;
        if (courseId >= 4 && courseId <= 6) {
            // Create mock course data for lessons 4, 5, 6
            String title = "";
            String description = "";
            int duration = 15; // Default duration in minutes
            
            switch(courseId.intValue()) {
                case 4:
                    title = "Începe un mini-compost acasă";
                    description = "Transformă resturile în viață nouă";
                    break;
                case 5:
                    title = "Plantează ceva (chiar și într-un ghiveci!)";
                    description = "Conectează-te cu natura prin plantat";
                    break;
                case 6:
                    title = "Adună gunoiul din cartierul tău";
                    description = "Fă diferența în comunitatea ta";
                    break;
            }
            
            course = new CourseDto(courseId, title, description, duration);
        } else {
            // For existing courses (1, 2, 3), fetch from database
            course = courseService.getCourseById(courseId);
        }
        
        model.addAttribute("course", course);
        
        // Check if user has already completed this course quiz
        boolean quizCompleted = false;
        if (currentUser != null) {
            quizCompleted = quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), courseId);
            model.addAttribute("quizCompleted", quizCompleted);
        }
        
        // Return different templates based on courseId
        switch(courseId.intValue()) {
            case 1:
                return "curs";
            case 2:
                return "curs2";
            case 3:
                return "curs3";
            case 4:
                return "curs4";
            case 5:
                return "curs5";
            case 6:
                return "curs6";
            default:
                return "curs"; // Default to first course if ID not found
        }
    }
    
    @GetMapping("/curs2")
    public String getCourse2(Model model, @AuthenticationPrincipal User currentUser) {
        // Check if user has already completed this course quiz
        boolean lessonCompleted = false;
        if (currentUser != null) {
            lessonCompleted = quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), 2L);
            model.addAttribute("lessonCompleted", lessonCompleted);
        }
        
        return "curs2";
    }
    
    @GetMapping("/curs4")
    public String getCourse4(Model model, @AuthenticationPrincipal User currentUser) {
        // Check if user has already completed this course quiz
        boolean lessonCompleted = false;
        if (currentUser != null) {
            lessonCompleted = quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), 4L);
            model.addAttribute("lessonCompleted", lessonCompleted);
        }
        
        return "curs4";
    }

    @GetMapping("/curs5")
    public String getCourse5(Model model, @AuthenticationPrincipal User currentUser) {
        // Check if user has already completed this course quiz
        boolean lessonCompleted = false;
        if (currentUser != null) {
            lessonCompleted = quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), 5L);
            model.addAttribute("lessonCompleted", lessonCompleted);
        }
        
        return "curs5";
    }

    @GetMapping("/curs6")
    public String getCourse6(Model model, @AuthenticationPrincipal User currentUser) {
        // Check if user has already completed this course quiz
        boolean lessonCompleted = false;
        if (currentUser != null) {
            lessonCompleted = quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), 6L);
            model.addAttribute("lessonCompleted", lessonCompleted);
        }
        
        return "curs6";
    }
    
    @PostMapping("/quiz/{courseId}/submit")
    @ResponseBody
    public ResponseEntity<?> submitQuiz(@PathVariable Long courseId, 
                                     @RequestBody Map<String, String> answers,
                                     @AuthenticationPrincipal User currentUser) {
        Map<String, Object> response = new HashMap<>();
        
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Trebuie să fii autentificat pentru a trimite un quiz.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Check if user has already completed this course quiz
        boolean alreadyCompleted = quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), courseId);
        if (alreadyCompleted) {
            response.put("success", false);
            response.put("message", "Ai completat deja acest quiz și ai primit punctele.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Calculate score - number of correct answers
            int correctAnswers = Integer.parseInt(answers.get("correctAnswers"));
            int totalQuestions = Integer.parseInt(answers.get("totalQuestions"));
            
            // Get reflection text if provided
            String reflectionText = answers.get("reflectionText");
            
            // Award points - 10 points per correct answer
            int pointsEarned = correctAnswers * 10;
            
            // Add extra points for reflection text
            if (reflectionText != null && !reflectionText.trim().isEmpty()) {
                pointsEarned += 5; // Reflection points
            }
            
            // Add extra points for image upload
            String imageUploaded = answers.get("imageUploaded");
            if ("true".equals(imageUploaded)) {
                pointsEarned += 10; // Image points
            }
            
            // Update user points
            userService.addPoints(currentUser.getId(), pointsEarned);
            
            // Create a quiz result record with reflection text
            courseService.saveQuizResult(currentUser.getId(), courseId, correctAnswers, totalQuestions, pointsEarned, reflectionText, null);
            
            // Track lesson completion for challenges
            challengeTrackingService.trackUserAction(currentUser.getId(), "LESSON_COMPLETED", courseId);
            
            // Calculate new level
            int oldLevel = (currentUser.getPoints() - pointsEarned) / 50 + 1;
            int newLevel = (currentUser.getPoints()) / 50 + 1;
            boolean leveledUp = newLevel > oldLevel;
            
            // Return success response
            response.put("success", true);
            response.put("correctAnswers", correctAnswers);
            response.put("totalQuestions", totalQuestions);
            response.put("pointsEarned", pointsEarned);
            response.put("leveledUp", leveledUp);
            response.put("newLevel", newLevel);
            
            String message = "Felicitări! Ai răspuns corect la " + correctAnswers + " din " + 
                     totalQuestions + " întrebări și ai câștigat " + pointsEarned + " puncte!";
            
            if (leveledUp) {
                message += " Ai avansat la nivelul " + newLevel + "!";
            }
            
            response.put("message", message);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Eroare la procesarea quiz-ului: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
