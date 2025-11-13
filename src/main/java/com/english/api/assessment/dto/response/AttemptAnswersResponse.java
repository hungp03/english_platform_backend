package com.english.api.assessment.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.english.api.quiz.enums.QuizSkill;

public record AttemptAnswersResponse(
    UUID attemptId,
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
    Instant submittedAt,
    String contextText,
    String explanation,

    List<AttemptAnswerItem> answers
) {}
