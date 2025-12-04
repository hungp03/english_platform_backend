package com.english.api.assessment.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record WritingSubmissionResponse(
        UUID id,
        UUID attemptAnswerId,
        String questionContent,
        String answerText,
        Double aiTaskResponse,
        Double aiCoherence,
        Double aiGrammar,
        Double aiVocabulary,
        Double aiScore,
        String feedback,
        Instant createdAt
) {
}
