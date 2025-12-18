package com.english.api.assessment.event;

import com.english.api.assessment.model.WritingSubmission;
import com.english.api.assessment.repository.WritingSubmissionRepository;
import com.english.api.quiz.model.Question;
import com.english.api.quiz.model.Quiz;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WritingSubmissionEventListener {

    private final WritingSubmissionRepository submissionRepo;
    private final RestTemplate restTemplate;

    @Value("${n8n.webhook.writing.url}")
    private String n8nWritingWebhookUrl;

    @Value("${n8n.webhook.api-key}")
    private String n8nApiKey;

    /**
     * Listens for WritingSubmissionCreatedEvent and triggers n8n workflow
     * after the transaction commits, ensuring the submission is persisted in the database.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWritingSubmissionCreated(WritingSubmissionCreatedEvent event) {
        try {
            WritingSubmission submission = submissionRepo.findById(event.submissionId())
                    .orElseThrow(() -> new IllegalStateException("Submission not found after commit: " + event.submissionId()));

            Question question = submission.getAttemptAnswer().getQuestion();
            Quiz quiz = question.getQuiz();

            Map<String, Object> payload = Map.of(
                    "submissionId", submission.getId().toString(),
                    "writingText", submission.getAttemptAnswer().getAnswerText() != null
                            ? submission.getAttemptAnswer().getAnswerText() : "",
                    "context", buildContext(quiz, question)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + n8nApiKey);
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            log.info("Triggering n8n writing workflow for submission: {} (after commit)", submission.getId());
            restTemplate.postForEntity(n8nWritingWebhookUrl, requestEntity, Void.class);
            log.info("Successfully triggered n8n writing workflow for submission: {}", submission.getId());

        } catch (Exception e) {
            log.error("Failed to trigger n8n writing workflow for submission: {}",
                    event.submissionId(), e);
        }
    }

    private Map<String, Object> buildContext(Quiz quiz, Question question) {
        Map<String, Object> context = new HashMap<>();

        if (quiz.getContextText() != null) {
            context.put("quizContextText", quiz.getContextText());
        }
        context.put("questionContent", question.getContent());

        return context;
    }
}
