package com.english.api.quiz.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

import com.english.api.quiz.model.enums.QuizSkill;

public record QuizSectionUpdateRequest(
    @NotBlank String name,
    String description,
    QuizSkill skill,
    @NotNull UUID quizTypeId
) {}
