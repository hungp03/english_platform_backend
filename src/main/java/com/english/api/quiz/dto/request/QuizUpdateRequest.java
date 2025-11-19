package com.english.api.quiz.dto.request;

import com.english.api.quiz.model.enums.QuizStatus;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record QuizUpdateRequest(
        @Size(max = 255) String title,
        @Size(max = 2000) String description,
        QuizStatus status,
        // QuizSkill skill,
        UUID quizTypeId,
        UUID quizSectionId,
        String contextText,
        String questionText,
        String explanation

) {
}