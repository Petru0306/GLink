package com.greenlink.greenlink.service;

import com.greenlink.greenlink.dto.CourseDto;
import com.greenlink.greenlink.model.Course;
import com.greenlink.greenlink.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

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
}
