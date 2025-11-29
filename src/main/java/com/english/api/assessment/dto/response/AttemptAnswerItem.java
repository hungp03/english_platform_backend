package com.english.api.assessment.dto.response;

import java.util.List;
import java.util.UUID;

public record AttemptAnswerItem(
        UUID questionId,
        String questionContent,
        Integer orderIndex,
        String explanation,
        UUID selectedOptionId,
        String selectedOptionContent,
        List<OptionBrief> correctOptions,
        Boolean isCorrect,
        String answerText,
        List<OptionReview> options
) {
}
