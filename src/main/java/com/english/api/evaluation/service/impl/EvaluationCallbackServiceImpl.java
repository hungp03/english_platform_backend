package com.english.api.evaluation.service.impl;

import com.english.api.evaluation.dto.request.EvaluationCallbackRequest;
import com.english.api.evaluation.dto.response.AckResponse;
import com.english.api.evaluation.entity.EvaluationJob;
import com.english.api.evaluation.entity.EvaluationStatus;
import com.english.api.evaluation.repository.EvaluationJobRepository;
import com.english.api.evaluation.service.EvaluationCallbackService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EvaluationCallbackServiceImpl implements EvaluationCallbackService {

    private final EvaluationJobRepository repo;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AckResponse handleCallback(EvaluationCallbackRequest payload, String idempotencyKey, String rawPayload, long timestampMillis, boolean signatureOk) {
        String eventId = payload.eventId();
        if (eventId == null || eventId.isBlank()) {
            // Fallback to idempotency key if eventId is missing
            eventId = idempotencyKey;
        }
        if (eventId == null || eventId.isBlank()) {
            return new AckResponse("error", "Missing eventId and idempotency key");
        }

        // Idempotency check
        if (repo.existsByEventId(eventId)) {
            return new AckResponse("duplicate", "Already processed");
        }

        EvaluationJob job = new EvaluationJob();
        job.setEventId(eventId);
        job.setProvider(payload.provider());
        job.setAttemptId(payload.attemptId());
        job.setQuizId(payload.quizId());
        job.setUserId(payload.userId());
        job.setStatus(parseStatus(payload.status()));
        job.setOverallScore(payload.overallScore());
        job.setModel(payload.model());
        job.setLatencyMs(payload.latencyMs());
        job.setMessage(payload.message());
        job.setSignatureOk(signatureOk);
        job.setRawPayload(rawPayload);
        job.setReceivedAt(Instant.ofEpochMilli(timestampMillis));
        job.setFinishedAt(payload.finishedAt());

        try {
            if (payload.metrics() != null) {
                job.setMetricsJson(objectMapper.writeValueAsString(payload.metrics()));
            }
            if (payload.items() != null) {
                job.setItemsJson(objectMapper.writeValueAsString(payload.items()));
            }
        } catch (JsonProcessingException e) {
            // store nothing, still accept
        }

        repo.save(job);

        // === OPTIONAL: integrate with your Attempt domain here ===
        if (job.getStatus() == EvaluationStatus.COMPLETED || job.getStatus() == EvaluationStatus.PARTIAL) {
            onCompleted(job);
        } else if (job.getStatus() == EvaluationStatus.FAILED) {
            onFailed(job);
        }

        return new AckResponse("ok", "received");
    }

    private EvaluationStatus parseStatus(String s) {
        if (s == null) return EvaluationStatus.PARTIAL;
        String u = s.toUpperCase();
        if (Objects.equals(u, "COMPLETED")) return EvaluationStatus.COMPLETED;
        if (Objects.equals(u, "FAILED")) return EvaluationStatus.FAILED;
        if (Objects.equals(u, "PENDING")) return EvaluationStatus.PENDING;
        return EvaluationStatus.PARTIAL;
        }

    // Hook to push result into Attempt/AttemptDetail
    private void onCompleted(EvaluationJob job) {
        // TODO: inject AttemptService and update evaluation results
        // e.g., attemptService.applyEvaluation(job.getAttemptId(), job.getOverallScore(), job.getMetricsJson(), job.getItemsJson());
    }

    private void onFailed(EvaluationJob job) {
        // TODO: optional failure handling, notifications, retry markers, etc.
    }
}
