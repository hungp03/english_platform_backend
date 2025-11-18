package com.english.api.assessment.dto.request;

import java.util.UUID;

public record AICallbackSpeakingRequest(
    UUID submissionId,
    String transcript,
    Double aiFluency,
    Double aiPronunciation,
    Double aiGrammar,
    Double aiVocabulary,
    Double aiScore,
    String feedback
) {}
