package com.english.api.assessment.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record SpeakingSubmissionResponse(
        UUID id,
        UUID attemptAnswerId,
        String questionContent,
        String audioUrl,
        String transcript,
        Double aiFluency,
        Double aiPronunciation,
        Double aiGrammar,
        Double aiVocabulary,
        Double aiScore,
        String feedback,
        Instant createdAt
) {
}
