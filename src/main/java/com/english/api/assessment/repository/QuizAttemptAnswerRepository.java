package com.english.api.assessment.repository;

import com.english.api.assessment.model.QuizAttemptAnswer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswer, UUID> {
    Optional<QuizAttemptAnswer> findByAttempt_IdAndQuestion_Id(UUID attemptId, UUID questionId);
    @EntityGraph(attributePaths = {"question", "selectedOption"})
    List<QuizAttemptAnswer> findByAttempt_Id(UUID attemptId);
}
