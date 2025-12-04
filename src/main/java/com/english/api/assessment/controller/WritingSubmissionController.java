package com.english.api.assessment.controller;

import com.english.api.assessment.dto.request.AICallbackWritingRequest;
import com.english.api.assessment.dto.response.WritingSubmissionResponse;
import com.english.api.assessment.dto.response.WritingSubmissionsWithMetadataResponse;
import com.english.api.assessment.service.WritingSubmissionService;
import com.english.api.common.exception.AccessDeniedException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assessment")
public class WritingSubmissionController {

    private final WritingSubmissionService writingSubmissionService;

    @Value("${n8n.callback.secret}")
    private String n8nCallbackSecret;

    @GetMapping("/writing-submissions/{submissionId}")
    public ResponseEntity<WritingSubmissionResponse> getSubmission(@PathVariable UUID submissionId) {
        return ResponseEntity.ok(writingSubmissionService.getSubmission(submissionId));
    }

    @PostMapping("/attempts/{attemptId}/answers/{answerId}/writing")
    public ResponseEntity<WritingSubmissionResponse> submitWriting(
            @PathVariable UUID attemptId,
            @PathVariable UUID answerId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(writingSubmissionService.submitWriting(attemptId, answerId));
    }

    @GetMapping("/attempts/{attemptId}/answers/{answerId}/writing")
    public ResponseEntity<WritingSubmissionResponse> getSubmissionByAnswer(
            @PathVariable UUID attemptId,
            @PathVariable UUID answerId) {
        return ResponseEntity.ok(writingSubmissionService.getSubmissionByAnswer(attemptId, answerId).orElse(null));
    }

    @GetMapping("/attempts/{attemptId}/writing-submissions")
    public ResponseEntity<WritingSubmissionsWithMetadataResponse> getSubmissionsByAttemptId(@PathVariable UUID attemptId) {
        return ResponseEntity.ok(writingSubmissionService.getSubmissionsWithMetadata(attemptId));
    }

    @PostMapping("/writing-submissions/{submissionId}/retry")
    public ResponseEntity<WritingSubmissionResponse> retryGrading(@PathVariable UUID submissionId) {
        return ResponseEntity.ok(writingSubmissionService.retryGrading(submissionId));
    }

    @DeleteMapping("/writing-submissions/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable UUID submissionId) {
        writingSubmissionService.deleteSubmission(submissionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ai-callback/writing")
    public ResponseEntity<Void> handleAICallback(
            @RequestHeader("X-N8N-Secret") String secret,
            @Valid @RequestBody AICallbackWritingRequest request) {

        if (!secret.equals(n8nCallbackSecret)) {
            throw new AccessDeniedException("Invalid callback secret");
        }

        writingSubmissionService.handleAICallback(request);
        return ResponseEntity.ok().build();
    }
}
