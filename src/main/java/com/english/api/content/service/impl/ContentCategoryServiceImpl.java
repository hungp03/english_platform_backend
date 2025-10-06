package com.english.api.content.service.impl;

import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.content.dto.request.CategoryCreateRequest;
import com.english.api.content.dto.request.CategoryUpdateRequest;
import com.english.api.content.dto.response.CategoryResponse;
import com.english.api.content.model.ContentCategory;
import com.english.api.content.repository.ContentCategoryRepository;
import com.english.api.content.service.ContentCategoryService;
import com.english.api.content.util.SlugUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentCategoryServiceImpl implements ContentCategoryService {

    private final ContentCategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse create(CategoryCreateRequest req) {
        String slug = (req.getSlug() != null && !req.getSlug().isBlank())
                ? SlugUtil.toSlug(req.getSlug())
                : SlugUtil.toSlug(req.getName());
        if (!SlugUtil.isSeoFriendly(slug)) {
            throw new IllegalArgumentException("Slug is not SEO-friendly");
        }
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Slug already exists");
        }
        ContentCategory category = ContentCategory.builder()
                .name(req.getName())
                .slug(slug)
                .description(req.getDescription())
                .build();
        category = categoryRepository.save(category);
        return toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse update(UUID id, CategoryUpdateRequest req) {
        ContentCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (req.getName() != null) cat.setName(req.getName());
        if (req.getDescription() != null) cat.setDescription(req.getDescription());
        if (req.getSlug() != null && !req.getSlug().isBlank()) {
            String slug = SlugUtil.toSlug(req.getSlug());
            if (!SlugUtil.isSeoFriendly(slug)) {
                throw new IllegalArgumentException("Slug is not SEO-friendly");
            }
            if (!slug.equalsIgnoreCase(cat.getSlug()) && categoryRepository.existsBySlug(slug)) {
                throw new IllegalArgumentException("Slug already exists");
            }
            cat.setSlug(slug);
        }
        cat = categoryRepository.save(cat);
        return toResponse(cat);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryResponse get(UUID id) {
        ContentCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return toResponse(cat);
    }

    @Override
    public Page<CategoryResponse> list(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::toResponse);
    }

    private CategoryResponse toResponse(ContentCategory c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .createdAt(c.getCreatedAt())
                .build();
    }
}