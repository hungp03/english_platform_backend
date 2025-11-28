package com.english.api.assessment.dto.response;

import com.english.api.quiz.model.enums.QuizSkill;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AttemptAnswersResponse(
        UUID attemptId,
        UUID quizId,
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
        Instant submittedAt,
        Integer completionTimeSeconds,
        String contextText,
        String explanation,
        List<AttemptAnswerItem> answers
) {
}
