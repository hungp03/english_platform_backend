package com.english.api.content.service;

import com.english.api.content.dto.request.CategoryCreateRequest;
import com.english.api.content.dto.request.CategoryUpdateRequest;
import com.english.api.content.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ContentCategoryService {
    CategoryResponse create(CategoryCreateRequest req);
    CategoryResponse update(UUID id, CategoryUpdateRequest req);
    void delete(UUID id);
    CategoryResponse get(UUID id);
    Page<CategoryResponse> list(Pageable pageable);
}