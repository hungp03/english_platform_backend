package com.english.api.auth.dto.request;


public record LinkGoogleAccountRequest(
        String idToken,
        String authorizationCode,
        String redirectUri
) {}
