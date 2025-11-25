package com.english.api.assessment.repository;

import com.english.api.assessment.model.WritingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WritingSubmissionRepository extends JpaRepository<WritingSubmission, UUID> {
    Optional<WritingSubmission> findByAttemptAnswer_Id(UUID attemptAnswerId);

    boolean existsByAttemptAnswer_Id(UUID attemptAnswerId);

    @Query("SELECT w FROM WritingSubmission w JOIN FETCH w.attemptAnswer aa WHERE aa.attempt.id = :attemptId")
    List<WritingSubmission> findByAttemptAnswer_Attempt_Id(@Param("attemptId") UUID attemptId);
}
