package com.english.api.blog.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.english.api.blog.dto.request.PostCreateRequest;
import com.english.api.blog.dto.request.PostFilterRequest;
import com.english.api.blog.dto.request.PostUpdateRequest;
import com.english.api.blog.dto.response.PostResponse;
import com.english.api.blog.dto.response.PublicPostDetailResponse;
import com.english.api.common.dto.PaginationResponse;

public interface BlogPostService {
    PostResponse create(PostCreateRequest req, boolean canPublish);
    PostResponse update(UUID id, PostUpdateRequest req);
    void delete(UUID id);
    PostResponse get(UUID id);
    PaginationResponse search(PostFilterRequest filter, Pageable pageable, boolean includeUnpublished);
    PostResponse publish(UUID id);
    PostResponse unpublish(UUID id);

    // public
    PaginationResponse publicList(PostFilterRequest filter, Pageable pageable);
    PublicPostDetailResponse publicDetailBySlug(String slug);
}
