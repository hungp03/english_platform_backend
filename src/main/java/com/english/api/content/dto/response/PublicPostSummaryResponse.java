package com.english.api.content.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PublicPostSummaryResponse {
    private UUID id;
    private String title;
    private String slug;
    private boolean published;
    private Instant publishedAt;
    private Instant createdAt;
    private List<CategoryResponse> categories;
}