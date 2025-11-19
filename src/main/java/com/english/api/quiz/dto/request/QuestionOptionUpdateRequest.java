package com.english.api.quiz.dto.request;

import jakarta.validation.constraints.Size;

public record QuestionOptionUpdateRequest(
        @Size(max = 1000) String content,
        Boolean correct,
        @Size(max = 2000) String explanation,
        Integer orderIndex
) {
}