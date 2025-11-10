package com.english.api.evaluation.repository;

import com.english.api.evaluation.entity.EvaluationJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EvaluationJobRepository extends JpaRepository<EvaluationJob, UUID> {

    Optional<EvaluationJob> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}
