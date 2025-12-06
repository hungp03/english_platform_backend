package com.english.api.assessment.dto;

public record CorrectionItem(
        String original,
        String corrected,
        String type,
        String explanation
) {
}
