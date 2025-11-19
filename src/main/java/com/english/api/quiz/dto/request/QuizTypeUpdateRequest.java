package com.english.api.quiz.dto.request;

import jakarta.validation.constraints.Size;

public record QuizTypeUpdateRequest(
        @Size(max = 255) String name,
        @Size(max = 512) String description
) {
}