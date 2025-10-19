package com.english.api.course.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/4/2025
 */

public record LessonResponse(
        UUID id,
        String title,
        String kind,
        Integer estimatedMin,
        Integer position,
        Boolean isFree,
        Boolean published,
        JsonNode content,
        UUID primaryMediaId,
        List<UUID> attachmentIds
) {}
