package com.english.api.quiz.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuestionOptionCreateRequest(
        @NotBlank @Size(max = 1000) String content,
        @NotNull Boolean correct,
        @Size(max = 2000) String explanation,
        @Min(1) Integer orderIndex
) {
}