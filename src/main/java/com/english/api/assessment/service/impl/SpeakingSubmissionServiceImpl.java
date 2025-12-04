package com.english.api.assessment.service.impl;

import com.english.api.assessment.dto.request.AICallbackSpeakingRequest;
import com.english.api.assessment.dto.response.SpeakingSubmissionResponse;
import com.english.api.assessment.dto.response.SpeakingSubmissionsWithMetadataResponse;
import com.english.api.assessment.event.SpeakingSubmissionCreatedEvent;
import com.english.api.assessment.mapper.SpeakingSubmissionMapper;
import com.english.api.assessment.model.QuizAttempt;
import com.english.api.common.dto.MediaUploadResponse;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.common.service.MediaService;
import com.english.api.assessment.model.QuizAttemptAnswer;
import com.english.api.assessment.model.SpeakingSubmission;
import com.english.api.assessment.repository.QuizAttemptAnswerRepository;
import com.english.api.assessment.repository.QuizAttemptRepository;
import com.english.api.assessment.repository.SpeakingSubmissionRepository;
import com.english.api.assessment.service.SpeakingSubmissionService;
import com.english.api.auth.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpeakingSubmissionServiceImpl implements SpeakingSubmissionService {

    private final SpeakingSubmissionRepository submissionRepo;
    private final QuizAttemptRepository attemptRepo;
    private final QuizAttemptAnswerRepository answerRepo;
    private final SpeakingSubmissionMapper mapper;
    private final MediaService mediaService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public SpeakingSubmissionResponse uploadAndSubmitAudio(UUID attemptId, UUID answerId, MultipartFile audioFile) throws IOException {
        UUID userId = SecurityUtil.getCurrentUserId();

        // Validate attempt and answer ownership
        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + attemptId));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Not authorized to access this attempt");
        }

        QuizAttemptAnswer answer = answerRepo.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found: " + answerId));

        if (!answer.getAttempt().getId().equals(attemptId)) {
            throw new ResourceInvalidException("Answer does not belong to the specified attempt");
        }

        // Check if submission already exists (1-1 relationship)
        if (submissionRepo.existsByAttemptAnswer_Id(answerId)) {
            throw new ResourceAlreadyExistsException("A speaking submission already exists for this answer. Use retryGrading to reprocess or delete the existing submission first.");
        }

        // Validate audio file
        if (audioFile.isEmpty()) {
            throw new ResourceInvalidException("Audio file is empty");
        }

        // Check file type
        String contentType = audioFile.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new ResourceInvalidException("File must be an audio file");
        }

        // Upload audio to S3
        String folder = String.format("speaking_assessments/%s/%s", attemptId, answerId);
        MediaUploadResponse uploadResponse = mediaService.uploadFile(audioFile, folder);

        log.info("Uploaded audio for attempt {} answer {} to S3: {}", attemptId, answerId, uploadResponse.url());

        // Create submission with uploaded audio URL
        SpeakingSubmission submission = SpeakingSubmission.builder()
                .attemptAnswer(answer)
                .audioUrl(uploadResponse.url())
                .build();

        SpeakingSubmission saved = submissionRepo.save(submission);

        // Publish event - trigger will run after transaction commits
        eventPublisher.publishEvent(new SpeakingSubmissionCreatedEvent(saved.getId()));

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SpeakingSubmissionResponse getSubmission(UUID submissionId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        SpeakingSubmission submission = submissionRepo.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found or not authorized: " + submissionId));

        return mapper.toResponse(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SpeakingSubmissionResponse> getSubmissionByAnswer(UUID attemptId, UUID answerId) {
        UUID userId = SecurityUtil.getCurrentUserId();

        // Validate attemptId-answerId relationship exists
        if (!answerRepo.existsByIdAndAttempt_Id(answerId, attemptId)) {
            throw new ResourceNotFoundException("Answer not found or does not belong to the attempt");
        }

        return submissionRepo.findByAttemptAnswerIdAndUserId(answerId, userId)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SpeakingSubmissionsWithMetadataResponse getSubmissionsWithMetadata(UUID attemptId) {
        UUID userId = SecurityUtil.getCurrentUserId();

        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + attemptId));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Not authorized to access this attempt");
        }

        List<SpeakingSubmissionResponse> submissions = submissionRepo.findByAttemptAnswer_Attempt_Id(attemptId).stream()
                .map(mapper::toResponse)
                .toList();

        return mapper.toSubmissionsWithMetadataResponse(attempt, submissions);
    }

    @Override
    @Transactional
    public SpeakingSubmissionResponse retryGrading(UUID submissionId) {
        SpeakingSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + submissionId));

        UUID userId = SecurityUtil.getCurrentUserId();
        if (!submission.getAttemptAnswer().getAttempt().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Not authorized to access this submission");
        }

        if (submission.getAiScore() != null) {
            throw new ResourceInvalidException("Submission already graded successfully");
        }

        // Publish event - trigger will run after transaction commits
        eventPublisher.publishEvent(new SpeakingSubmissionCreatedEvent(submission.getId()));

        return mapper.toResponse(submission);
    }

    @Override
    @Transactional
    public void deleteSubmission(UUID submissionId) {
        SpeakingSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + submissionId));

        UUID userId = SecurityUtil.getCurrentUserId();
        if (!submission.getAttemptAnswer().getAttempt().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Not authorized to delete this submission");
        }

        submissionRepo.delete(submission);
    }

    @Override
    @Transactional
    public void handleAICallback(AICallbackSpeakingRequest request) {
        SpeakingSubmission submission = submissionRepo.findById(request.submissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + request.submissionId()));

        mapper.updateFromAICallback(request, submission);

        submissionRepo.save(submission);
    }
}
