package com.english.api.assessment.service.impl;

import com.english.api.assessment.dto.request.AICallbackWritingRequest;
import com.english.api.assessment.dto.response.WritingSubmissionResponse;
import com.english.api.assessment.mapper.WritingSubmissionMapper;
import com.english.api.assessment.model.QuizAttempt;
import com.english.api.assessment.model.QuizAttemptAnswer;
import com.english.api.assessment.model.WritingSubmission;
import com.english.api.assessment.repository.QuizAttemptAnswerRepository;
import com.english.api.assessment.repository.QuizAttemptRepository;
import com.english.api.assessment.repository.WritingSubmissionRepository;
import com.english.api.assessment.service.WritingSubmissionService;
import com.english.api.auth.util.SecurityUtil;
import com.english.api.quiz.model.Question;
import com.english.api.quiz.model.Quiz;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WritingSubmissionServiceImpl implements WritingSubmissionService {

    private final WritingSubmissionRepository submissionRepo;
    private final QuizAttemptRepository attemptRepo;
    private final QuizAttemptAnswerRepository answerRepo;
    private final RestTemplate restTemplate;
    private final WritingSubmissionMapper mapper;

    @Value("${n8n.webhook.writing.url}")
    private String n8nWritingWebhookUrl;

    @Override
    @Transactional(readOnly = true)
    public WritingSubmissionResponse getSubmission(UUID submissionId) {
        WritingSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + submissionId));

        UUID userId = SecurityUtil.getCurrentUserId();
        if (!submission.getAttemptAnswer().getAttempt().getUser().getId().equals(userId)) {
            throw new SecurityException("Not authorized to access this submission");
        }

        return mapper.toResponse(submission);
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
                .map(mapper::toResponse);
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

        // Trigger n8n workflow again for retry
        triggerWritingGrading(submission);

        return mapper.toResponse(submission);
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

        mapper.updateFromAICallback(request, submission);

        submissionRepo.save(submission);
    }

    /**
     * Trigger n8n workflow to grade writing submission
     */
    @Async
    private void triggerWritingGrading(WritingSubmission submission) {
        try {
            QuizAttemptAnswer answer = submission.getAttemptAnswer();
            Question question = answer.getQuestion();
            Quiz quiz = question.getQuiz();

            Map<String, Object> payload = Map.of(
                    "submissionId", submission.getId().toString(),
                    "writingText", answer.getAnswerText() != null ? answer.getAnswerText() : "",
                    "context", buildContext(quiz, question)
            );

            log.info("Triggering n8n writing workflow for submission: {}", submission.getId());
            restTemplate.postForEntity(n8nWritingWebhookUrl, payload, Void.class);
            log.info("Successfully triggered n8n writing workflow for submission: {}", submission.getId());

        } catch (Exception e) {
            // Log error but don't fail the submission
            log.error("Failed to trigger n8n writing workflow for submission: {}",
                    submission.getId(), e);
        }
    }

    /**
     * Build context for n8n workflow
     */
    private Map<String, Object> buildContext(Quiz quiz, Question question) {
        Map<String, Object> context = new HashMap<>();

        if (quiz.getContextText() != null) {
            context.put("quizContextText", quiz.getContextText());
        }
        if (quiz.getQuestionText() != null) {
            context.put("questionText", quiz.getQuestionText());
        }
        context.put("questionContent", question.getContent());

        // TODO: Add imageUrls, wordLimit, taskType from quiz metadata if needed
        // context.put("taskType", "task1"); // or "task2"
        // context.put("wordLimit", 250);
        // if (!imageUrls.isEmpty()) {
        //     context.put("imageUrls", imageUrls);
        // }

        return context;
    }
}
