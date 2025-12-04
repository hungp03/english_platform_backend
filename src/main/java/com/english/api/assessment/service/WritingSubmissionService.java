package com.english.api.assessment.service;

import com.english.api.assessment.dto.request.AICallbackWritingRequest;
import com.english.api.assessment.dto.response.WritingSubmissionResponse;
import com.english.api.assessment.dto.response.WritingSubmissionsWithMetadataResponse;

import java.util.Optional;
import java.util.UUID;

public interface WritingSubmissionService {
    WritingSubmissionResponse submitWriting(UUID attemptId, UUID answerId);

    WritingSubmissionResponse getSubmission(UUID submissionId);

    Optional<WritingSubmissionResponse> getSubmissionByAnswer(UUID attemptId, UUID answerId);

    WritingSubmissionsWithMetadataResponse getSubmissionsWithMetadata(UUID attemptId);

    WritingSubmissionResponse retryGrading(UUID submissionId);

    void deleteSubmission(UUID submissionId);

    void handleAICallback(AICallbackWritingRequest request);
}
