package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    // Find all answers for a specific quiz result
    List<QuizAnswer> findByQuizResultId(Long quizResultId);
    
    // Find answers for a specific quiz result and question number
    List<QuizAnswer> findByQuizResultIdAndQuestionNumber(Long quizResultId, int questionNumber);
    
    // Find all answers for quiz results belonging to a specific user
    List<QuizAnswer> findByQuizResult_UserId(Long userId);
    
    // Find all answers for quiz results belonging to a specific quiz
    List<QuizAnswer> findByQuizResult_QuizId(Long quizId);
    
    // Find all answers for a specific user and quiz
    List<QuizAnswer> findByQuizResult_UserIdAndQuizResult_QuizId(Long userId, Long quizId);
}
