package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.QuizResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    // Găsește toate rezultatele unui utilizator
    List<QuizResult> findByUserId(Long userId);

    // Găsește rezultate pentru un quiz specific
    List<QuizResult> findByQuizId(Long quizId);

    // Găsește rezultatele unui utilizator pentru un quiz specific
    List<QuizResult> findByUserIdAndQuizId(Long userId, Long quizId);

    // Găsește rezultate cu paginare
    Page<QuizResult> findByUserId(Long userId, Pageable pageable);

    // Găsește cel mai recent rezultat al unui utilizator pentru un quiz
    QuizResult findTopByUserIdAndQuizIdOrderByCompletedAtDesc(Long userId, Long quizId);

    // Găsește rezultatele din ultima săptămână
    List<QuizResult> findByCompletedAtAfter(LocalDateTime date);

    // Calculează scorul mediu pentru un quiz
    @Query("SELECT AVG(qr.score) FROM QuizResult qr WHERE qr.quiz.id = :quizId")
    Double calculateAverageScoreForQuiz(Long quizId);

    // Găsește cele mai bune scoruri pentru un quiz
    @Query("SELECT qr FROM QuizResult qr WHERE qr.quiz.id = :quizId ORDER BY qr.score DESC")
    List<QuizResult> findTopScoresForQuiz(Long quizId, Pageable pageable);

    // Verifică dacă un utilizator a completat un quiz
    boolean existsByUserIdAndQuizId(Long userId, Long quizId);

    // Numără câte quiz-uri a completat un utilizator
    long countByUserId(Long userId);
}