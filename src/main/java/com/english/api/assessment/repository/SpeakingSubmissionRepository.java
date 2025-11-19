package com.english.api.assessment.repository;

import com.english.api.assessment.model.SpeakingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpeakingSubmissionRepository extends JpaRepository<SpeakingSubmission, UUID> {
    Optional<SpeakingSubmission> findByAttemptAnswer_Id(UUID attemptAnswerId);

    boolean existsByAttemptAnswer_Id(UUID attemptAnswerId);
}
