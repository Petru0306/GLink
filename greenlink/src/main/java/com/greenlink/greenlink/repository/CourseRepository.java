package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    // Adaugă metode personalizate dacă este necesar
}
