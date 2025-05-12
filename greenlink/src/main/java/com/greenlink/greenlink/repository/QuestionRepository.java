package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    // Găsește toate întrebările pentru un quiz specific
    List<Question> findByQuizId(Long quizId);

    // Găsește întrebările după tip
    List<Question> findByType(Question.QuestionType type);

    // Găsește întrebări după quiz și tip
    List<Question> findByQuizIdAndType(Long quizId, Question.QuestionType type);

    // Numără întrebările dintr-un quiz
    long countByQuizId(Long quizId);
}