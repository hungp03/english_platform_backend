package com.english.api.quiz.dto.response;

import java.time.Instant;
import java.util.UUID;

public record QuizTypeResponse(
        UUID id,
        String code,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}