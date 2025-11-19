package com.english.api.assessment.repository;

import com.english.api.assessment.model.WritingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WritingSubmissionRepository extends JpaRepository<WritingSubmission, UUID> {
    Optional<WritingSubmission> findByAttemptAnswer_Id(UUID attemptAnswerId);

    boolean existsByAttemptAnswer_Id(UUID attemptAnswerId);
}
