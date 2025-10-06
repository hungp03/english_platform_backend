package com.english.api.content.service;

import com.english.api.content.dto.request.PostCreateRequest;
import com.english.api.content.dto.request.PostFilterRequest;
import com.english.api.content.dto.request.PostUpdateRequest;
import com.english.api.content.dto.response.PostResponse;
import com.english.api.content.dto.response.PublicPostDetailResponse;
import com.english.api.content.dto.response.PublicPostSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ContentPostService {
    PostResponse create(PostCreateRequest req, boolean canPublish);
    PostResponse update(UUID id, PostUpdateRequest req);
    void delete(UUID id);
    PostResponse get(UUID id);
    Page<PostResponse> search(PostFilterRequest filter, Pageable pageable, boolean includeUnpublished);
    PostResponse publish(UUID id);
    PostResponse unpublish(UUID id);

    // public
    Page<PublicPostSummaryResponse> publicList(PostFilterRequest filter, Pageable pageable);
    PublicPostDetailResponse publicDetailBySlug(String slug);
}