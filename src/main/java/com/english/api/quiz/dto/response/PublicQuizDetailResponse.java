package com.english.api.quiz.dto.response;

import com.english.api.quiz.model.enums.QuizSkill;

import java.util.List;
import java.util.UUID;

public record PublicQuizDetailResponse(
        UUID id,
        String title,
        String description,
        UUID quizTypeId,
        String quizTypeName,
        UUID quizSectionId,
        String quizSectionName,
        String contextText,
        String explanation,
        QuizSkill skill,
        List<PublicQuestion> questions
) {
}
