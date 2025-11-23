package com.english.api.assessment.controller;

import com.english.api.assessment.dto.request.AICallbackWritingRequest;
import com.english.api.assessment.dto.response.WritingSubmissionResponse;
import com.english.api.assessment.service.WritingSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
    public WritingSubmissionResponse getSubmission(@PathVariable UUID submissionId) {
        return writingSubmissionService.getSubmission(submissionId);
    }

    @GetMapping("/attempts/{attemptId}/answers/{answerId}/writing")
    public WritingSubmissionResponse getSubmissionByAnswer(@PathVariable UUID attemptId, @PathVariable UUID answerId) {
        return writingSubmissionService.getSubmissionByAnswer(attemptId, answerId).orElse(null);
    }

    @PostMapping("/writing-submissions/{submissionId}/retry")
    public WritingSubmissionResponse retryGrading(@PathVariable UUID submissionId) {
        return writingSubmissionService.retryGrading(submissionId);
    }

    @DeleteMapping("/writing-submissions/{submissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubmission(@PathVariable UUID submissionId) {
        writingSubmissionService.deleteSubmission(submissionId);
    }

    @PostMapping("/ai-callback/writing")
    @ResponseStatus(HttpStatus.OK)
    public void handleAICallback(
            @RequestHeader("X-N8N-Secret") String secret,
            @Valid @RequestBody AICallbackWritingRequest request) {

        if (!secret.equals(n8nCallbackSecret)) {
            throw new SecurityException("Invalid callback secret");
        }

        writingSubmissionService.handleAICallback(request);
    }
}
