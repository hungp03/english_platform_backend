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
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<SpeakingSubmissionResponse> uploadAndSubmitAudio(
            @PathVariable UUID attemptId,
            @PathVariable UUID answerId,
            @RequestParam("audio") MultipartFile audioFile) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(speakingSubmissionService.uploadAndSubmitAudio(attemptId, answerId, audioFile));
    }

    @GetMapping("/speaking-submissions/{submissionId}")
    public ResponseEntity<SpeakingSubmissionResponse> getSubmission(@PathVariable UUID submissionId) {
        return ResponseEntity.ok(speakingSubmissionService.getSubmission(submissionId));
    }

    @GetMapping("/attempts/{attemptId}/answers/{answerId}/speaking")
    public ResponseEntity<SpeakingSubmissionResponse> getSubmissionByAnswer(
            @PathVariable UUID attemptId,
            @PathVariable UUID answerId) {
        return ResponseEntity.ok(speakingSubmissionService.getSubmissionByAnswer(attemptId, answerId).orElse(null));
    }

    @GetMapping("/attempts/{attemptId}/speaking-submissions")
    public ResponseEntity<SpeakingSubmissionsWithMetadataResponse> getSubmissionsByAttemptId(@PathVariable UUID attemptId) {
        return ResponseEntity.ok(speakingSubmissionService.getSubmissionsWithMetadata(attemptId));
    }

    @PostMapping("/speaking-submissions/{submissionId}/retry")
    public ResponseEntity<SpeakingSubmissionResponse> retryGrading(@PathVariable UUID submissionId) {
        return ResponseEntity.ok(speakingSubmissionService.retryGrading(submissionId));
    }

    @DeleteMapping("/speaking-submissions/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable UUID submissionId) {
        speakingSubmissionService.deleteSubmission(submissionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ai-callback/speaking")
    public ResponseEntity<Void> handleAICallback(
            @RequestHeader("X-N8N-Secret") String secret,
            @Valid @RequestBody AICallbackSpeakingRequest request) {

        if (!secret.equals(n8nCallbackSecret)) {
            throw new AccessDeniedException("Invalid callback secret");
        }

        speakingSubmissionService.handleAICallback(request);
        return ResponseEntity.ok().build();
    }
}
