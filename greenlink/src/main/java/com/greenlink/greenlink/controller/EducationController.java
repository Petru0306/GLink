package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.CourseDto;
import com.greenlink.greenlink.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/educatie")
public class EducationController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    public String getAllCourses(Model model) {
        List<CourseDto> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        return "educatie"; // Returnează fișierul education.html
    }

    @GetMapping("/course/{courseId}")
    public String getCourseDetails(@PathVariable Long courseId, Model model) {
        CourseDto course = courseService.getCourseById(courseId);
        model.addAttribute("course", course);
        return "course-details"; // Returnează fișierul course-details.html
    }
}
