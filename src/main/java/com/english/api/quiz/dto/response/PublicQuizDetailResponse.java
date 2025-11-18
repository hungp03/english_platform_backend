package com.english.api.quiz.dto.response;

import java.util.List;
import java.util.UUID;

import com.english.api.quiz.model.enums.QuizSkill;

public record PublicQuizDetailResponse(
    UUID id,
    String title,
    String description,
    UUID quizTypeId,
    String quizTypeName,
    UUID quizSectionId,
    String quizSectionName,
    String contextText,
    QuizSkill skill,
    List<PublicQuestion> questions
) {}
