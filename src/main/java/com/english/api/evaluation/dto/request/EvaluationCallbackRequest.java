package com.english.api.evaluation.dto.request;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record EvaluationCallbackRequest(
        String eventId,
        String provider,
        UUID jobId,
        UUID attemptId,
        UUID quizId,
        UUID userId,
        String status,           // COMPLETED | FAILED | PARTIAL
        Double overallScore,
        Map<String, Object> metrics,
        List<EvaluationItemResult> items,
        String model,
        Long latencyMs,
        String message,
        Instant finishedAt
) {}
