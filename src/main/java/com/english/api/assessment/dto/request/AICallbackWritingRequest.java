package com.english.api.assessment.dto.request;

import java.util.UUID;

public record AICallbackWritingRequest(
        UUID submissionId,
        Double aiTaskResponse,
        Double aiCoherence,
        Double aiGrammar,
        Double aiVocabulary,
        Double aiScore,
        String feedback
) {
}
