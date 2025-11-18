package com.english.api.quiz.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.english.api.quiz.model.enums.QuizSkill;
import com.english.api.quiz.model.enums.QuizStatus;

public record QuizResponse(
        UUID id,
        String title,
        String description,
        QuizStatus status,
        QuizSkill skill,
        // String skill,
        UUID quizTypeId,
        UUID quizSectionId,
        String quizSectionName,
        String quizTypeName,
        Instant createdAt,
        Instant updatedAt,
        String contextText,
        String questionText,
        String explanation
) {}