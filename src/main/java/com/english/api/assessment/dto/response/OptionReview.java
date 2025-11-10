package com.english.api.assessment.dto.response;

import java.util.UUID;

/** Full option for review screen (both MCQ and others) */
public record OptionReview(
    UUID id,
    String content,
    boolean correct,
    boolean selected
) {}
