package com.english.api.content.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PostResponse {
    private UUID id;
    private UUID authorId;
    private String title;
    private String slug;
    private String bodyMd;
    private boolean published;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CategoryResponse> categories;
}