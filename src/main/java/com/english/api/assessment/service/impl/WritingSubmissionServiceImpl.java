package com.english.api.assessment.service.impl;

import com.english.api.assessment.dto.request.AICallbackWritingRequest;
import com.english.api.assessment.dto.response.WritingSubmissionResponse;
import com.english.api.assessment.dto.response.WritingSubmissionsWithMetadataResponse;
import com.english.api.assessment.event.WritingSubmissionCreatedEvent;
import com.english.api.assessment.mapper.WritingSubmissionMapper;
import com.english.api.assessment.model.QuizAttempt;
import com.english.api.assessment.model.QuizAttemptAnswer;
import com.english.api.assessment.model.WritingSubmission;
import com.english.api.assessment.repository.QuizAttemptAnswerRepository;
import com.english.api.assessment.repository.QuizAttemptRepository;
import com.english.api.assessment.repository.WritingSubmissionRepository;
import com.english.api.assessment.service.WritingSubmissionService;
import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WritingSubmissionServiceImpl implements WritingSubmissionService {

    private final WritingSubmissionRepository submissionRepo;
    private final QuizAttemptRepository attemptRepo;
    private final QuizAttemptAnswerRepository answerRepo;
    private final WritingSubmissionMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public WritingSubmissionResponse submitWriting(UUID attemptId, UUID answerId) {
        UUID userId = SecurityUtil.getCurrentUserId();

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

        if (submissionRepo.existsByAttemptAnswer_Id(answerId)) {
            throw new ResourceAlreadyExistsException("A writing submission already exists for this answer");
        }

        if (answer.getAnswerText() == null || answer.getAnswerText().isBlank()) {
            throw new ResourceInvalidException("Answer text is empty");
        }

        WritingSubmission submission = WritingSubmission.builder()
                .attemptAnswer(answer)
                .build();

        WritingSubmission saved = submissionRepo.save(submission);

        eventPublisher.publishEvent(new WritingSubmissionCreatedEvent(saved.getId()));

        log.info("Created WritingSubmission for answer: {}", answerId);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WritingSubmissionResponse getSubmission(UUID submissionId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        WritingSubmission submission = submissionRepo.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found or not authorized: " + submissionId));

        return mapper.toResponse(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WritingSubmissionResponse> getSubmissionByAnswer(UUID attemptId, UUID answerId) {
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
    public WritingSubmissionsWithMetadataResponse getSubmissionsWithMetadata(UUID attemptId) {
        UUID userId = SecurityUtil.getCurrentUserId();

        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + attemptId));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Not authorized to access this attempt");
        }

        List<WritingSubmissionResponse> submissions = submissionRepo.findByAttemptAnswer_Attempt_Id(attemptId).stream()
                .map(mapper::toResponse)
                .toList();

        return mapper.toSubmissionsWithMetadataResponse(attempt, submissions);
    }

    @Override
    @Transactional
    public WritingSubmissionResponse retryGrading(UUID submissionId) {
        WritingSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + submissionId));

        UUID userId = SecurityUtil.getCurrentUserId();
        if (!submission.getAttemptAnswer().getAttempt().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Not authorized to access this submission");
        }

        if (submission.getAiScore() != null) {
            throw new ResourceInvalidException("Submission already graded successfully");
        }

        // Publish event - trigger will run after transaction commits
        eventPublisher.publishEvent(new WritingSubmissionCreatedEvent(submission.getId()));

        return mapper.toResponse(submission);
    }

    @Override
    @Transactional
    public void deleteSubmission(UUID submissionId) {
        WritingSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + submissionId));

        UUID userId = SecurityUtil.getCurrentUserId();
        if (!submission.getAttemptAnswer().getAttempt().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Not authorized to delete this submission");
        }

        submissionRepo.delete(submission);
    }

    @Override
    @Transactional
    public void handleAICallback(AICallbackWritingRequest request) {
        WritingSubmission submission = submissionRepo.findById(request.submissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + request.submissionId()));

        mapper.updateFromAICallback(request, submission);

        submissionRepo.save(submission);
    }
}
