package com.english.api.quiz.dto.response;

import java.util.UUID;

public record QuestionOptionResponse(
        UUID id,
        String content,
        boolean correct,
        Integer orderIndex
) {
}