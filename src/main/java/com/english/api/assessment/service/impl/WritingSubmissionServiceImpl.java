package com.english.api.assessment.service.impl;

import com.english.api.assessment.dto.request.AICallbackWritingRequest;
import com.english.api.assessment.dto.request.WritingSubmissionRequest;
import com.english.api.assessment.dto.response.WritingSubmissionResponse;
import com.english.api.assessment.model.QuizAttempt;
import com.english.api.assessment.model.QuizAttemptAnswer;
import com.english.api.assessment.model.WritingSubmission;
import com.english.api.assessment.repository.QuizAttemptAnswerRepository;
import com.english.api.assessment.repository.QuizAttemptRepository;
import com.english.api.assessment.repository.WritingSubmissionRepository;
import com.english.api.assessment.service.WritingSubmissionService;
import com.english.api.auth.util.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WritingSubmissionServiceImpl implements WritingSubmissionService {

    private final WritingSubmissionRepository submissionRepo;
    private final QuizAttemptRepository attemptRepo;
    private final QuizAttemptAnswerRepository answerRepo;

    @Override
    @Transactional
    public WritingSubmissionResponse submitWriting(UUID attemptId, UUID answerId, WritingSubmissionRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();

        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Attempt not found: " + attemptId));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new SecurityException("Not authorized to access this attempt");
        }

        QuizAttemptAnswer answer = answerRepo.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("Answer not found: " + answerId));

        if (!answer.getAttempt().getId().equals(attemptId)) {
            throw new IllegalArgumentException("Answer does not belong to the specified attempt");
        }

        // Check if submission already exists (1-1 relationship)
        if (submissionRepo.existsByAttemptAnswer_Id(answerId)) {
            throw new IllegalStateException("A writing submission already exists for this answer. Use retryGrading to reprocess or delete the existing submission first.");
        }

        WritingSubmission submission = WritingSubmission.builder()
                .attemptAnswer(answer)
                .build();

        WritingSubmission saved = submissionRepo.save(submission);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WritingSubmissionResponse getSubmission(UUID submissionId) {
        WritingSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + submissionId));
        
        UUID userId = SecurityUtil.getCurrentUserId();
        if (!submission.getAttemptAnswer().getAttempt().getUser().getId().equals(userId)) {
            throw new SecurityException("Not authorized to access this submission");
        }
        
        return toResponse(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WritingSubmissionResponse> getSubmissionByAnswer(UUID attemptId, UUID answerId) {
        UUID userId = SecurityUtil.getCurrentUserId();

        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Attempt not found: " + attemptId));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new SecurityException("Not authorized to access this attempt");
        }

        QuizAttemptAnswer answer = answerRepo.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("Answer not found: " + answerId));

        if (!answer.getAttempt().getId().equals(attemptId)) {
            throw new IllegalArgumentException("Answer does not belong to the specified attempt");
        }

        return submissionRepo.findByAttemptAnswer_Id(answerId)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public WritingSubmissionResponse retryGrading(UUID submissionId) {
        WritingSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + submissionId));
        
        UUID userId = SecurityUtil.getCurrentUserId();
        if (!submission.getAttemptAnswer().getAttempt().getUser().getId().equals(userId)) {
            throw new SecurityException("Not authorized to access this submission");
        }
        
        return toResponse(submission);
    }

    @Override
    @Transactional
    public void deleteSubmission(UUID submissionId) {
        WritingSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + submissionId));
        
        UUID userId = SecurityUtil.getCurrentUserId();
        if (!submission.getAttemptAnswer().getAttempt().getUser().getId().equals(userId)) {
            throw new SecurityException("Not authorized to delete this submission");
        }
        
        submissionRepo.delete(submission);
    }

    @Override
    @Transactional
    public void handleAICallback(AICallbackWritingRequest request) {
        WritingSubmission submission = submissionRepo.findById(request.submissionId())
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + request.submissionId()));
        
        submission.setAiTaskResponse(request.aiTaskResponse());
        submission.setAiCoherence(request.aiCoherence());
        submission.setAiGrammar(request.aiGrammar());
        submission.setAiVocabulary(request.aiVocabulary());
        submission.setAiScore(request.aiScore());
        submission.setFeedback(request.feedback());
        
        submissionRepo.save(submission);
    }

    private WritingSubmissionResponse toResponse(WritingSubmission submission) {
        return WritingSubmissionResponse.builder()
                .id(submission.getId())
                .attemptAnswerId(submission.getAttemptAnswer().getId())
                .aiTaskResponse(submission.getAiTaskResponse())
                .aiCoherence(submission.getAiCoherence())
                .aiGrammar(submission.getAiGrammar())
                .aiVocabulary(submission.getAiVocabulary())
                .aiScore(submission.getAiScore())
                .feedback(submission.getFeedback())
                .createdAt(submission.getCreatedAt())
                .build();
    }
}
