package com.english.api.assessment.repository;

import com.english.api.assessment.model.SpeakingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpeakingSubmissionRepository extends JpaRepository<SpeakingSubmission, UUID> {
    Optional<SpeakingSubmission> findByAttemptAnswer_Id(UUID attemptAnswerId);

    boolean existsByAttemptAnswer_Id(UUID attemptAnswerId);

    @Query("SELECT s FROM SpeakingSubmission s JOIN FETCH s.attemptAnswer aa WHERE aa.attempt.id = :attemptId")
    List<SpeakingSubmission> findByAttemptAnswer_Attempt_Id(@Param("attemptId") UUID attemptId);
}
