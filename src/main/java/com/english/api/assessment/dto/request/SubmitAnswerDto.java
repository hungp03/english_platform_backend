package com.english.api.assessment.dto.request;

import java.util.UUID;

public record SubmitAnswerDto(
        UUID questionId,
        UUID selectedOptionId,
        String answerText
) {
}
