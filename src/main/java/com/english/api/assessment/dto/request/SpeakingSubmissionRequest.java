package com.english.api.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SpeakingSubmissionRequest(
        @NotBlank String audioUrl
) {
}
