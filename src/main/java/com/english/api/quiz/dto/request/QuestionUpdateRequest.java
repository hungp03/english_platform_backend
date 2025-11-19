package com.english.api.quiz.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record QuestionUpdateRequest(
        UUID quizId,
        @Size(max = 2000) String content,
        Integer orderIndex,
        List<QuestionOptionCreateRequest> options
) {
}