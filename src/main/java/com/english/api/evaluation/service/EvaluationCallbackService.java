package com.english.api.evaluation.service;

import com.english.api.evaluation.dto.request.EvaluationCallbackRequest;
import com.english.api.evaluation.dto.response.AckResponse;

public interface EvaluationCallbackService {

    AckResponse handleCallback(EvaluationCallbackRequest payload,
                               String idempotencyKey,
                               String rawPayload,
                               long timestampMillis,
                               boolean signatureOk);
}
