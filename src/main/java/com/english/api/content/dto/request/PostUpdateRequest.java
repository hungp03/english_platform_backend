package com.english.api.content.dto.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PostUpdateRequest {
    private String title;
    private String slug;
    private String bodyMd;
    private List<UUID> categoryIds;
}