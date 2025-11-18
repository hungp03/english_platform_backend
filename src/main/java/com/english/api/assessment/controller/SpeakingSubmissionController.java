package com.english.api.assessment.controller;

import com.english.api.assessment.dto.request.AICallbackSpeakingRequest;
import com.english.api.assessment.dto.request.SpeakingSubmissionRequest;
import com.english.api.assessment.dto.response.SpeakingSubmissionResponse;
import com.english.api.assessment.service.SpeakingSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assessment")
public class SpeakingSubmissionController {

    private final SpeakingSubmissionService speakingSubmissionService;

    @PostMapping("/attempts/{attemptId}/answers/{answerId}/speaking")
    public SpeakingSubmissionResponse submitAudio(
            @PathVariable UUID attemptId,
            @PathVariable UUID answerId,
            @Valid @RequestBody SpeakingSubmissionRequest request) {
        return speakingSubmissionService.submitAudio(attemptId, answerId, request);
    }

    @GetMapping("/speaking-submissions/{submissionId}")
    public SpeakingSubmissionResponse getSubmission(@PathVariable UUID submissionId) {
        return speakingSubmissionService.getSubmission(submissionId);
    }

    @GetMapping("/attempts/{attemptId}/answers/{answerId}/speaking")
    public SpeakingSubmissionResponse getSubmissionByAnswer(
            @PathVariable UUID attemptId,
            @PathVariable UUID answerId) {
        return speakingSubmissionService.getSubmissionByAnswer(attemptId, answerId)
                .orElse(null);
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
    public void handleAICallback(@Valid @RequestBody AICallbackSpeakingRequest request) {
        speakingSubmissionService.handleAICallback(request);
    }
}
