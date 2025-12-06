package com.english.api.assessment.dto.request;

import com.english.api.assessment.dto.CorrectionItem;
import com.english.api.assessment.dto.CorrectionsDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.UUID;

public record AICallbackWritingRequest(
        UUID submissionId,
        Double aiTaskResponse,
        Double aiCoherence,
        Double aiGrammar,
        Double aiVocabulary,
        Double aiScore,
        String feedback,
        @JsonDeserialize(using = CorrectionsDeserializer.class)
        List<CorrectionItem> corrections
) {
}
