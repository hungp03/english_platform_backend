package com.english.api.assessment.service;

import com.english.api.assessment.dto.request.AICallbackWritingRequest;
import com.english.api.assessment.dto.response.WritingSubmissionResponse;

import java.util.Optional;
import java.util.UUID;

public interface WritingSubmissionService {
    WritingSubmissionResponse getSubmission(UUID submissionId);

    Optional<WritingSubmissionResponse> getSubmissionByAnswer(UUID attemptId, UUID answerId);

    WritingSubmissionResponse retryGrading(UUID submissionId);

    void deleteSubmission(UUID submissionId);

    void handleAICallback(AICallbackWritingRequest request);
}
