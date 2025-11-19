package com.english.api.order.service.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PayPalAccessTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn
) {}
