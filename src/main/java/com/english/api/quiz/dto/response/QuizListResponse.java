package com.english.api.quiz.dto.response;

import com.english.api.quiz.model.enums.QuizSkill;
import com.english.api.quiz.model.enums.QuizStatus;

import java.time.Instant;
import java.util.UUID;

public record QuizListResponse(
        UUID id,
        String title,
        QuizStatus status,
        QuizSkill skill,
        // String skill,
        UUID quizTypeId,
        UUID quizSectionId,
        String quizSectionName,
        String quizTypeName,
        Instant createdAt,
        Instant updatedAt
) {
}