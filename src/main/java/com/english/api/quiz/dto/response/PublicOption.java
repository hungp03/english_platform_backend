package com.english.api.quiz.dto.response;

import java.util.UUID;

public record PublicOption(
    UUID id,
    String content,
    Integer orderIndex
) {}
