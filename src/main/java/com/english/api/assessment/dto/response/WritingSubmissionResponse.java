package com.english.api.assessment.dto.response;

import com.english.api.assessment.dto.CorrectionItem;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
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
        List<CorrectionItem> corrections,
        Instant createdAt
) {
}
