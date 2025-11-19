package com.english.api.assessment.repository;

import com.english.api.assessment.model.QuizAttempt;
import com.english.api.assessment.model.enums.QuizAttemptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    Page<QuizAttempt> findByUser_Id(UUID userId, Pageable pageable);

    Page<QuizAttempt> findByQuiz_Id(UUID quizId, Pageable pageable);

    Page<QuizAttempt> findByQuiz_IdAndUser_Id(UUID quizId, UUID userId, Pageable pageable);
}
