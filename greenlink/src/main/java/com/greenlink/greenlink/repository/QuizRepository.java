package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}