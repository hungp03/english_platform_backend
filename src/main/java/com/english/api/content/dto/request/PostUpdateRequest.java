package com.english.api.content.dto.request;

import java.util.List;
import java.util.UUID;

public record PostUpdateRequest(
    String title,
    String slug,
    String bodyMd,
    List<UUID> categoryIds
) {}
