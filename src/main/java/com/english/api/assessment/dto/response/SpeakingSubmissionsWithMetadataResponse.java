package com.english.api.assessment.dto.response;

import com.english.api.quiz.model.enums.QuizSkill;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record SpeakingSubmissionsWithMetadataResponse(
        UUID attemptId,
        UUID quizId,
        String quizType,
        String quizSection,
        String quizName,
        QuizSkill skill,
        String status,
        Integer totalQuestions,
        Instant startedAt,
        Instant submittedAt,
        String contextText,
        List<SpeakingSubmissionResponse> submissions
) {
}
