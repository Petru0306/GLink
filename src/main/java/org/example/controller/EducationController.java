package com.greenlink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/education")
public class EducationController {

    @GetMapping
    public String educationPage(Model model) {
        List<String> courses = List.of(
                "Curs 1 - Reciclare",
                "Curs 2 - Energie verde",
                "Curs 3 - Reducerea amprentei de carbon"
        );
        model.addAttribute("courses", courses);
        return "education"; // Returnează education.html din templates
    }

    @GetMapping("/course/{courseId}")
    public String courseDetails(@PathVariable Long courseId, Model model) {
        model.addAttribute("courseTitle", "Curs despre reciclare");
        model.addAttribute("courseContent", "Detalii despre cursul de reciclare...");
        return "course-details"; // Returnează course-details.html
    }
}
