package com.english.api.quiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

import com.english.api.quiz.model.enums.QuizStatus;

public record QuizCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        @NotNull QuizStatus status,
        // @NotNull QuizSkill skill,
        @NotNull UUID quizTypeId,
        UUID quizSectionId,
        String contextText,
        String questionText, 
        String explanation 
) {}