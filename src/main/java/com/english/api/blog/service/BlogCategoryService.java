package com.english.api.blog.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.english.api.blog.dto.request.BlogCategoryCreateRequest;
import com.english.api.blog.dto.request.BlogCategoryUpdateRequest;
import com.english.api.blog.dto.response.BlogCategoryResponse;
import com.english.api.common.dto.PaginationResponse;

public interface BlogCategoryService {
    BlogCategoryResponse create(BlogCategoryCreateRequest req);
    BlogCategoryResponse update(UUID id, BlogCategoryUpdateRequest req);
    void delete(UUID id);
    BlogCategoryResponse get(UUID id);
    PaginationResponse list(Pageable pageable);
}
