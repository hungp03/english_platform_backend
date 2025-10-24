package com.english.api.quiz.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuestionResponse(
        UUID id,
        UUID quizId,
        String content,
        Integer orderIndex,
        List<QuestionOptionResponse> options,
        Instant createdAt,
        Instant updatedAt
) {}