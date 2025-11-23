package com.english.api.assessment.service.impl;

import com.english.api.assessment.dto.request.AICallbackSpeakingRequest;
import com.english.api.assessment.dto.request.SpeakingSubmissionRequest;
import com.english.api.assessment.dto.response.SpeakingSubmissionResponse;
import com.english.api.assessment.model.QuizAttempt;
import com.english.api.assessment.model.QuizAttemptAnswer;
import com.english.api.assessment.model.SpeakingSubmission;
import com.english.api.assessment.repository.QuizAttemptAnswerRepository;
import com.english.api.assessment.repository.QuizAttemptRepository;
import com.english.api.assessment.repository.SpeakingSubmissionRepository;
import com.english.api.assessment.service.SpeakingSubmissionService;
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
public class SpeakingSubmissionServiceImpl implements SpeakingSubmissionService {

    private final SpeakingSubmissionRepository submissionRepo;
    private final QuizAttemptRepository attemptRepo;
    private final QuizAttemptAnswerRepository answerRepo;
    private final RestTemplate restTemplate;

    @Value("${n8n.webhook.speaking.url}")
    private String n8nSpeakingWebhookUrl;

    @Override
    @Transactional
    public SpeakingSubmissionResponse submitAudio(UUID attemptId, UUID answerId, SpeakingSubmissionRequest request) {
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
            throw new IllegalStateException("A speaking submission already exists for this answer. Use retryGrading to reprocess or delete the existing submission first.");
        }

        SpeakingSubmission submission = SpeakingSubmission.builder()
                .attemptAnswer(answer)
                .audioUrl(request.audioUrl())
                .build();

        SpeakingSubmission saved = submissionRepo.save(submission);

        // Trigger n8n workflow asynchronously
        triggerSpeakingGrading(saved);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SpeakingSubmissionResponse getSubmission(UUID submissionId) {
        SpeakingSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + submissionId));

        UUID userId = SecurityUtil.getCurrentUserId();
        if (!submission.getAttemptAnswer().getAttempt().getUser().getId().equals(userId)) {
            throw new SecurityException("Not authorized to access this submission");
        }

        return toResponse(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SpeakingSubmissionResponse> getSubmissionByAnswer(UUID attemptId, UUID answerId) {
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
    public SpeakingSubmissionResponse retryGrading(UUID submissionId) {
        SpeakingSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + submissionId));

        UUID userId = SecurityUtil.getCurrentUserId();
        if (!submission.getAttemptAnswer().getAttempt().getUser().getId().equals(userId)) {
            throw new SecurityException("Not authorized to access this submission");
        }

        // Trigger n8n workflow again for retry
        triggerSpeakingGrading(submission);

        return toResponse(submission);
    }

    @Override
    @Transactional
    public void deleteSubmission(UUID submissionId) {
        SpeakingSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + submissionId));

        UUID userId = SecurityUtil.getCurrentUserId();
        if (!submission.getAttemptAnswer().getAttempt().getUser().getId().equals(userId)) {
            throw new SecurityException("Not authorized to delete this submission");
        }

        submissionRepo.delete(submission);
    }

    @Override
    @Transactional
    public void handleAICallback(AICallbackSpeakingRequest request) {
        SpeakingSubmission submission = submissionRepo.findById(request.submissionId())
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + request.submissionId()));

        submission.setTranscript(request.transcript());
        submission.setAiFluency(request.aiFluency());
        submission.setAiPronunciation(request.aiPronunciation());
        submission.setAiGrammar(request.aiGrammar());
        submission.setAiVocabulary(request.aiVocabulary());
        submission.setAiScore(request.aiScore());
        submission.setFeedback(request.feedback());

        submissionRepo.save(submission);
    }

    private SpeakingSubmissionResponse toResponse(SpeakingSubmission submission) {
        return SpeakingSubmissionResponse.builder()
                .id(submission.getId())
                .attemptAnswerId(submission.getAttemptAnswer().getId())
                .audioUrl(submission.getAudioUrl())
                .transcript(submission.getTranscript())
                .aiFluency(submission.getAiFluency())
                .aiPronunciation(submission.getAiPronunciation())
                .aiGrammar(submission.getAiGrammar())
                .aiVocabulary(submission.getAiVocabulary())
                .aiScore(submission.getAiScore())
                .feedback(submission.getFeedback())
                .createdAt(submission.getCreatedAt())
                .build();
    }

    /**
     * Trigger n8n workflow to grade speaking submission
     */
    @Async
    private void triggerSpeakingGrading(SpeakingSubmission submission) {
        try {
            QuizAttemptAnswer answer = submission.getAttemptAnswer();
            Question question = answer.getQuestion();
            Quiz quiz = question.getQuiz();

            Map<String, Object> payload = Map.of(
                    "submissionId", submission.getId().toString(),
                    "audioUrl", submission.getAudioUrl(),
                    "context", buildContext(quiz, question)
            );

            log.info("Triggering n8n speaking workflow for submission: {}", submission.getId());
            restTemplate.postForEntity(n8nSpeakingWebhookUrl, payload, Void.class);
            log.info("Successfully triggered n8n speaking workflow for submission: {}", submission.getId());

        } catch (Exception e) {
            // Log error but don't fail the submission
            log.error("Failed to trigger n8n speaking workflow for submission: {}",
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

        // TODO: Add imageUrls from MediaAsset if needed
        // List<String> imageUrls = getImageUrls(quiz);
        // if (!imageUrls.isEmpty()) {
        //     context.put("imageUrls", imageUrls);
        // }

        return context;
    }
}
