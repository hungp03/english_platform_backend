package com.english.api.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentCreateRequest {
    private UUID parentId; // nullable
    @NotBlank
    private String bodyMd;
}