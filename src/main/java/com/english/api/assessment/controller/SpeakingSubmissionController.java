package com.english.api.assessment.controller;

import com.english.api.assessment.dto.request.AICallbackSpeakingRequest;
import com.english.api.assessment.dto.response.SpeakingSubmissionResponse;
import com.english.api.assessment.dto.response.SpeakingSubmissionsWithMetadataResponse;
import com.english.api.assessment.service.SpeakingSubmissionService;
import com.english.api.common.exception.AccessDeniedException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assessment")
public class SpeakingSubmissionController {

    private final SpeakingSubmissionService speakingSubmissionService;

    @Value("${n8n.callback.secret}")
    private String n8nCallbackSecret;

    /**
     * Upload audio file and create speaking submission
     * Accepts audio file directly, uploads to S3, and creates submission automatically
     */
    @PostMapping(value = "/attempts/{attemptId}/answers/{answerId}/speaking",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SpeakingSubmissionResponse uploadAndSubmitAudio(
            @PathVariable UUID attemptId,
            @PathVariable UUID answerId,
            @RequestParam("audio") MultipartFile audioFile) throws IOException {
        return speakingSubmissionService.uploadAndSubmitAudio(attemptId, answerId, audioFile);
    }

    @GetMapping("/speaking-submissions/{submissionId}")
    public SpeakingSubmissionResponse getSubmission(@PathVariable UUID submissionId) {
        return speakingSubmissionService.getSubmission(submissionId);
    }

    @GetMapping("/attempts/{attemptId}/answers/{answerId}/speaking")
    public SpeakingSubmissionResponse getSubmissionByAnswer(@PathVariable UUID attemptId, @PathVariable UUID answerId) {
        return speakingSubmissionService.getSubmissionByAnswer(attemptId, answerId).orElse(null);
    }

    @GetMapping("/attempts/{attemptId}/speaking-submissions")
    public SpeakingSubmissionsWithMetadataResponse getSubmissionsByAttemptId(@PathVariable UUID attemptId) {
        return speakingSubmissionService.getSubmissionsWithMetadata(attemptId);
    }

    @PostMapping("/speaking-submissions/{submissionId}/retry")
    public SpeakingSubmissionResponse retryGrading(@PathVariable UUID submissionId) {
        return speakingSubmissionService.retryGrading(submissionId);
    }

    @DeleteMapping("/speaking-submissions/{submissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubmission(@PathVariable UUID submissionId) {
        speakingSubmissionService.deleteSubmission(submissionId);
    }

    @PostMapping("/ai-callback/speaking")
    @ResponseStatus(HttpStatus.OK)
    public void handleAICallback(
            @RequestHeader("X-N8N-Secret") String secret,
            @Valid @RequestBody AICallbackSpeakingRequest request) {

        if (!secret.equals(n8nCallbackSecret)) {
            throw new AccessDeniedException("Invalid callback secret");
        }

        speakingSubmissionService.handleAICallback(request);
    }
}
