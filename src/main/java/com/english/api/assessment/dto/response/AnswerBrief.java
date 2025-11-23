package com.english.api.assessment.dto.response;

import java.util.UUID;

public record AnswerBrief(
        UUID answerId,
        UUID questionId
) {
}
