package com.english.api.auth.dto.response;

public record LinkAccountResponse(
        String message,
        String provider,
        String email
) {}
