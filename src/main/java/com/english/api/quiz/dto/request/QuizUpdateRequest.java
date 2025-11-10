package com.english.api.quiz.dto.request;

import jakarta.validation.constraints.Size;

import java.util.UUID;

import com.english.api.quiz.enums.QuizSkill;
import com.english.api.quiz.enums.QuizStatus;

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

) {}