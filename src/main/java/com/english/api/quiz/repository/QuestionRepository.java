package com.english.api.quiz.repository;

import com.english.api.quiz.model.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    @EntityGraph(attributePaths = {"options"})
    Page<Question> findByQuiz_IdOrderByOrderIndexAsc(UUID quizId, Pageable pageable);

    @EntityGraph(attributePaths = {"options"})
    Page<Question> findByQuiz_QuizSection_Id(UUID sectionId, Pageable pageable);
}
