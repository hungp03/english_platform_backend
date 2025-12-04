package com.english.api.assessment.repository;

import com.english.api.assessment.model.SpeakingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpeakingSubmissionRepository extends JpaRepository<SpeakingSubmission, UUID> {
    
    @Query("SELECT s FROM SpeakingSubmission s " +
           "JOIN FETCH s.attemptAnswer aa " +
           "JOIN FETCH aa.question " +
           "WHERE s.id = :id AND aa.attempt.user.id = :userId")
    Optional<SpeakingSubmission> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT s FROM SpeakingSubmission s " +
           "JOIN FETCH s.attemptAnswer aa " +
           "JOIN FETCH aa.question " +
           "WHERE aa.id = :attemptAnswerId AND aa.attempt.user.id = :userId")
    Optional<SpeakingSubmission> findByAttemptAnswerIdAndUserId(@Param("attemptAnswerId") UUID attemptAnswerId, @Param("userId") UUID userId);

    Optional<SpeakingSubmission> findByAttemptAnswer_Id(UUID attemptAnswerId);

    boolean existsByAttemptAnswer_Id(UUID attemptAnswerId);

    @Query("SELECT s FROM SpeakingSubmission s " +
           "JOIN FETCH s.attemptAnswer aa " +
           "JOIN FETCH aa.question " +
           "WHERE aa.attempt.id = :attemptId")
    List<SpeakingSubmission> findByAttemptAnswer_Attempt_Id(@Param("attemptId") UUID attemptId);
}
