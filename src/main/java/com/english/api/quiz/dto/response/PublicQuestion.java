package com.english.api.quiz.dto.response;

import java.util.List;
import java.util.UUID;

public record PublicQuestion(
    UUID id,
    Integer orderIndex,
    String content,
    // String contextText,
    // String imageUrl,
    // String audioUrl,
    List<PublicOption> options
) {}
