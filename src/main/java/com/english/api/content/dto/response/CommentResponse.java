package com.english.api.content.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CommentResponse {
    private UUID id;
    private UUID postId;
    private UUID parentId;
    private UUID authorId;
    private String bodyMd;
    private boolean published;
    private Instant createdAt;
    private Instant updatedAt;
}