package com.english.api.quiz.dto.response;

import java.util.UUID;

public record QuizSectionResponse(
        UUID id,
        String name,
        String description,
        String skill,
        UUID quizTypeId,
        String quizTypeName,
        String createdAt,
        String updatedAt
) {
}
