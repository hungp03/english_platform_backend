package com.english.api.assessment.repository;

import com.english.api.assessment.model.QuizAttemptAnswer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswer, UUID> {
    Optional<QuizAttemptAnswer> findByAttempt_IdAndQuestion_Id(UUID attemptId, UUID questionId);

    @EntityGraph(attributePaths = {"question", "selectedOption"})
    List<QuizAttemptAnswer> findByAttempt_Id(UUID attemptId);

    @EntityGraph(attributePaths = {"question", "selectedOption"})
    List<QuizAttemptAnswer> findByAttempt_IdIn(List<UUID> attemptIds);
}
