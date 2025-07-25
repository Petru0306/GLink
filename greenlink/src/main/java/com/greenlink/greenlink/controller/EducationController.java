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
        model.addAttribute("isAuthenticated", currentUser != null);
        if (currentUser != null) {
            List<Long> completedLessons = new ArrayList<>();
            for (long i = 1; i <= 6; i++) {
                if (quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), i)) {
                    completedLessons.add(i);
                }
            }
            ObjectMapper mapper = new ObjectMapper();
            try {
                String completedLessonsJson = mapper.writeValueAsString(completedLessons);
                model.addAttribute("completedLessons", completedLessonsJson);
            } catch (Exception e) {
                model.addAttribute("completedLessons", "[]");
            }
        }
        
        return "educatie"; 
    }

    @GetMapping("/curs/{courseId}")
    public String getCourseDetails(@PathVariable Long courseId, Model model, @AuthenticationPrincipal User currentUser) {
        CourseDto course;
        if (courseId >= 4 && courseId <= 6) {

            String title = "";
            String description = "";
            int duration = 15; 
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
            course = courseService.getCourseById(courseId);
        }
        
        model.addAttribute("course", course);
        
        boolean quizCompleted = false;
        if (currentUser != null) {
            quizCompleted = quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), courseId);
            model.addAttribute("quizCompleted", quizCompleted);
        }
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
                return "curs"; 
        }
    }
    
    @GetMapping("/curs2")
    public String getCourse2(Model model, @AuthenticationPrincipal User currentUser) {
        boolean lessonCompleted = false;
        if (currentUser != null) {
            lessonCompleted = quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), 2L);
            model.addAttribute("lessonCompleted", lessonCompleted);
        }
        
        return "curs2";
    }
    
    @GetMapping("/curs4")
    public String getCourse4(Model model, @AuthenticationPrincipal User currentUser) {
        boolean lessonCompleted = false;
        if (currentUser != null) {
            lessonCompleted = quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), 4L);
            model.addAttribute("lessonCompleted", lessonCompleted);
        }
        
        return "curs4";
    }

    @GetMapping("/curs5")
    public String getCourse5(Model model, @AuthenticationPrincipal User currentUser) {
        boolean lessonCompleted = false;
        if (currentUser != null) {
            lessonCompleted = quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), 5L);
            model.addAttribute("lessonCompleted", lessonCompleted);
        }
        
        return "curs5";
    }

    @GetMapping("/curs6")
    public String getCourse6(Model model, @AuthenticationPrincipal User currentUser) {
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
        
        boolean alreadyCompleted = quizResultRepository.existsByUserIdAndQuizId(currentUser.getId(), courseId);
        if (alreadyCompleted) {
            response.put("success", false);
            response.put("message", "Ai completat deja acest quiz și ai primit punctele.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            int correctAnswers = Integer.parseInt(answers.get("correctAnswers"));
            int totalQuestions = Integer.parseInt(answers.get("totalQuestions"));
            String reflectionText = answers.get("reflectionText");
            int pointsFromQuestions = correctAnswers * 5;
            int reflectionPoints = 0;
            if (reflectionText != null && !reflectionText.trim().isEmpty()) {
                reflectionPoints = 5;
            }
            int imagePoints = 0;
            String imageUploaded = answers.get("imageUploaded");
            if ("true".equals(imageUploaded)) {
                imagePoints = 10; 
            }
        
            int totalPointsEarned = pointsFromQuestions + reflectionPoints + imagePoints;
            
            String courseTitle;
            if (courseId >= 4 && courseId <= 6) {
                switch(courseId.intValue()) {
                    case 4:
                        courseTitle = "Începe un mini-compost acasă";
                        break;
                    case 5:
                        courseTitle = "Plantează ceva (chiar și într-un ghiveci!)";
                        break;
                    case 6:
                        courseTitle = "Adună gunoiul din cartierul tău";
                        break;
                    default:
                        courseTitle = "Lecția " + courseId;
                }
            } else {
                courseTitle = courseService.getCourseById(courseId).getTitle();
            }
            
            pointsService.awardLessonPoints(currentUser.getId(), courseId, courseTitle, totalPointsEarned);
            
            courseService.saveQuizResult(currentUser.getId(), courseId, correctAnswers, totalQuestions, totalPointsEarned, reflectionText, null);
          
            challengeTrackingService.trackUserAction(currentUser.getId(), "LESSON_COMPLETED", courseId);
          
            User updatedUser = userService.getUserById(currentUser.getId());
            int newLevel = updatedUser.getLevel();
            boolean leveledUp = newLevel > (updatedUser.getPoints() - totalPointsEarned) / 50 + 1;
            
            response.put("success", true);
            response.put("correctAnswers", correctAnswers);
            response.put("totalQuestions", totalQuestions);
            response.put("pointsEarned", totalPointsEarned);
            response.put("leveledUp", leveledUp);
            response.put("newLevel", newLevel);
            
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Felicitări! Ai câștigat ").append(totalPointsEarned).append(" puncte!");
            
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
