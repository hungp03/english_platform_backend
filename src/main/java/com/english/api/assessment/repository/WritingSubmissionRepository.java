package com.english.api.assessment.repository;

import com.english.api.assessment.model.WritingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WritingSubmissionRepository extends JpaRepository<WritingSubmission, UUID> {
    
    @Query("SELECT w FROM WritingSubmission w " +
           "JOIN FETCH w.attemptAnswer aa " +
           "JOIN FETCH aa.question " +
           "WHERE w.id = :id AND aa.attempt.user.id = :userId")
    Optional<WritingSubmission> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT w FROM WritingSubmission w " +
           "JOIN FETCH w.attemptAnswer aa " +
           "JOIN FETCH aa.question " +
           "WHERE aa.id = :attemptAnswerId AND aa.attempt.user.id = :userId")
    Optional<WritingSubmission> findByAttemptAnswerIdAndUserId(@Param("attemptAnswerId") UUID attemptAnswerId, @Param("userId") UUID userId);

    Optional<WritingSubmission> findByAttemptAnswer_Id(UUID attemptAnswerId);

    boolean existsByAttemptAnswer_Id(UUID attemptAnswerId);

    @Query("SELECT w FROM WritingSubmission w " +
           "JOIN FETCH w.attemptAnswer aa " +
           "JOIN FETCH aa.question " +
           "WHERE aa.attempt.id = :attemptId")
    List<WritingSubmission> findByAttemptAnswer_Attempt_Id(@Param("attemptId") UUID attemptId);
}
