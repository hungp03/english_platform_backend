package com.english.api.assessment.service;

import com.english.api.assessment.dto.request.AICallbackSpeakingRequest;
import com.english.api.assessment.dto.response.SpeakingSubmissionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public interface SpeakingSubmissionService {
    SpeakingSubmissionResponse uploadAndSubmitAudio(UUID attemptId, UUID answerId, MultipartFile audioFile) throws IOException;

    SpeakingSubmissionResponse getSubmission(UUID submissionId);

    Optional<SpeakingSubmissionResponse> getSubmissionByAnswer(UUID attemptId, UUID answerId);

    SpeakingSubmissionResponse retryGrading(UUID submissionId);

    void deleteSubmission(UUID submissionId);

    void handleAICallback(AICallbackSpeakingRequest request);
}
