package com.english.api.assessment.service;

import com.english.api.assessment.dto.request.AICallbackSpeakingRequest;
import com.english.api.assessment.dto.request.SpeakingSubmissionRequest;
import com.english.api.assessment.dto.response.SpeakingSubmissionResponse;

import java.util.Optional;
import java.util.UUID;

public interface SpeakingSubmissionService {
    SpeakingSubmissionResponse submitAudio(UUID attemptId, UUID answerId, SpeakingSubmissionRequest request);
    SpeakingSubmissionResponse getSubmission(UUID submissionId);
    Optional<SpeakingSubmissionResponse> getSubmissionByAnswer(UUID attemptId, UUID answerId);
    SpeakingSubmissionResponse retryGrading(UUID submissionId);
    void deleteSubmission(UUID submissionId);
    void handleAICallback(AICallbackSpeakingRequest request);
}
