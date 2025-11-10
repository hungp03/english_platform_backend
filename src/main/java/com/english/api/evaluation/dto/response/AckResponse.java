package com.english.api.evaluation.dto.response;

public record AckResponse(
        String status,   // ok | duplicate | error
        String message
) {}
