package com.english.api.quiz.dto.request;

import com.english.api.quiz.model.enums.QuizSkill;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record QuizSectionCreateRequest(
        @NotBlank String name,
        String description,
        QuizSkill skill,
        @NotNull UUID quizTypeId
) {
}
