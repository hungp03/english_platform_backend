
package com.english.api.assessment.dto.request;

import java.util.List;
import java.util.UUID;

public record SubmitAttemptRequest(
        UUID quizId,
        List<SubmitAnswerDto> answers
) {
}
