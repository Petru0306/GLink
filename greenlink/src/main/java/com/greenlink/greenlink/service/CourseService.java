package com.greenlink.greenlink.service;

import com.greenlink.greenlink.dto.CourseDto;
import com.greenlink.greenlink.model.Course;
import com.greenlink.greenlink.model.QuizResult;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.CourseRepository;
import com.greenlink.greenlink.repository.QuizResultRepository;
import com.greenlink.greenlink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private QuizResultRepository quizResultRepository;
    
    @Autowired
    private UserRepository userRepository;

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit"));
        
        // Create a new quiz result
        QuizResult quizResult = new QuizResult();
        quizResult.setUser(user);
        // We're using courseId as quizId since they correspond in our simplified model
        quizResult.setQuiz(null); // We don't have a Quiz entity for courses yet
        quizResult.setScore(correctAnswers);
        quizResult.setPointsEarned(pointsEarned);
        quizResult.setCompletedAt(LocalDateTime.now());
        
        return quizResultRepository.save(quizResult);
    }
}
