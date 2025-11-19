package com.english.api.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendNotificationRequest(
        @NotNull
        UUID userId,
        @NotBlank
        String title,
        @NotBlank
        String content
) {
}
