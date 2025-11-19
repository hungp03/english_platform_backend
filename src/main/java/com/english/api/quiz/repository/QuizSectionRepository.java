package com.english.api.quiz.repository;

import com.english.api.quiz.model.QuizSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizSectionRepository extends JpaRepository<QuizSection, UUID> {
    Page<QuizSection> findByQuizTypeId(UUID quizTypeId, Pageable pageable);

    List<QuizSection> findByQuizTypeId(UUID quizTypeId);
}
