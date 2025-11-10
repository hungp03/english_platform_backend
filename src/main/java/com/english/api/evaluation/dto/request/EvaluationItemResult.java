package com.english.api.evaluation.dto.request;

import java.util.Map;
import java.util.UUID;

public record EvaluationItemResult(
        UUID questionId,
        Double score,
        String verdict,
        String feedback,
        Map<String, Object> extra
) {}
