package com.english.api.quiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record QuestionCreateRequest(
        @NotNull UUID quizId,
        @NotBlank @Size(max = 2000) String content,
        @NotNull Integer orderIndex,
        List<QuestionOptionCreateRequest> options
) {}