package com.english.api.assessment.dto.response;

import com.english.api.quiz.model.enums.QuizSkill;

import java.time.Instant;
import java.util.UUID;

public record AttemptResponse(
        UUID id,
        UUID quizId,
        UUID userId,
        String quizType,
        String quizSection,
        String quizName,
        QuizSkill skill,
        String status,
        Integer totalQuestions,
        Integer totalCorrect,
        Double score,
        Double maxScore,
        Instant startedAt,
        Instant submittedAt
) {
}
