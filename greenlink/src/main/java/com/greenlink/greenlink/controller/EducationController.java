package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.CourseDto;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.QuizResultRepository;
import com.greenlink.greenlink.service.CourseService;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ChallengeTrackingService;
import com.greenlink.greenlink.service.PointsService;
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

    @Autowired
    private PointsService pointsService;

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
            
            // Calculate points: 5 points per correct answer
            int pointsFromQuestions = correctAnswers * 5;
            
            // Add points for reflection text ("Scrie")
            int reflectionPoints = 0;
            if (reflectionText != null && !reflectionText.trim().isEmpty()) {
                reflectionPoints = 5; // "Scrie" worth 5 points
            }
            
            // Add points for image upload ("Foto")
            int imagePoints = 0;
            String imageUploaded = answers.get("imageUploaded");
            if ("true".equals(imageUploaded)) {
                imagePoints = 10; // "Foto" worth 10 points
            }
            
            // Total points earned
            int totalPointsEarned = pointsFromQuestions + reflectionPoints + imagePoints;
            
            // Award points using PointsService
            pointsService.awardLessonPoints(currentUser.getId(), courseId, 
                courseService.getCourseById(courseId).getTitle(), totalPointsEarned);
            
            // Create a quiz result record with reflection text
            courseService.saveQuizResult(currentUser.getId(), courseId, correctAnswers, totalQuestions, totalPointsEarned, reflectionText, null);
            
            // Track lesson completion for challenges
            challengeTrackingService.trackUserAction(currentUser.getId(), "LESSON_COMPLETED", courseId);
            
            // Get updated user to check new level
            User updatedUser = userService.getUserById(currentUser.getId());
            int newLevel = updatedUser.getLevel();
            boolean leveledUp = newLevel > (updatedUser.getPoints() - totalPointsEarned) / 50 + 1;
            
            // Return success response
            response.put("success", true);
            response.put("correctAnswers", correctAnswers);
            response.put("totalQuestions", totalQuestions);
            response.put("pointsEarned", totalPointsEarned);
            response.put("leveledUp", leveledUp);
            response.put("newLevel", newLevel);
            
            // Build message showing points earned
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Felicitări! Ai câștigat ").append(totalPointsEarned).append(" puncte!");
            
            // Add point breakdown
            messageBuilder.append(" (Întrebări: ").append(pointsFromQuestions).append(" puncte");
            if (reflectionPoints > 0) {
                messageBuilder.append(", Scrie: ").append(reflectionPoints).append(" puncte");
            }
            if (imagePoints > 0) {
                messageBuilder.append(", Foto: ").append(imagePoints).append(" puncte");
            }
            messageBuilder.append(")");
            
            if (leveledUp) {
                messageBuilder.append(" Ai avansat la nivelul ").append(newLevel).append("!");
            }
            
            response.put("message", messageBuilder.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Eroare la procesarea quiz-ului: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
