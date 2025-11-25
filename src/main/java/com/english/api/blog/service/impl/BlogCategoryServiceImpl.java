package com.english.api.blog.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.english.api.blog.dto.request.BlogCategoryCreateRequest;
import com.english.api.blog.dto.request.BlogCategoryUpdateRequest;
import com.english.api.blog.dto.response.BlogCategoryResponse;
import com.english.api.blog.mapper.BlogMapper;
import com.english.api.blog.model.BlogCategory;
import com.english.api.blog.repository.BlogCategoryRepository;
import com.english.api.blog.repository.BlogPostRepository;
import com.english.api.blog.service.BlogCategoryService;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.CannotDeleteException;
import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.common.util.SlugUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlogCategoryServiceImpl implements BlogCategoryService {
    private final BlogCategoryRepository categoryRepository;
    private final BlogPostRepository postRepository;
    private final BlogMapper blogMapper;

    @Override
    @Transactional
    public BlogCategoryResponse create(BlogCategoryCreateRequest req) {
        String slug = generateSlug(req.slug(), req.name());
        validateSlug(slug);
        ensureSlugNotExists(slug);
        BlogCategory category = blogMapper.toEntity(req, slug);
        category = categoryRepository.save(category);
        return blogMapper.toResponse(category);
    }

    private String generateSlug(String providedSlug, String name) {
        return (providedSlug != null && !providedSlug.isBlank())
                ? SlugUtil.toSlug(providedSlug)
                : SlugUtil.toSlug(name);
    }

    private void validateSlug(String slug) {
        if (!SlugUtil.isSeoFriendly(slug)) {
            throw new ResourceInvalidException("Slug is not SEO-friendly");
        }
    }

    private void ensureSlugNotExists(String slug) {
        if (categoryRepository.existsBySlug(slug)) {
            throw new ResourceAlreadyExistsException("Slug already exists");
        }
    }

    @Override
    @Transactional
    public BlogCategoryResponse update(UUID id, BlogCategoryUpdateRequest req) {
        BlogCategory category = findCategoryById(id);
        applyUpdates(category, req);
        return blogMapper.toResponse(category);
    }

    private BlogCategory findCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private void applyUpdates(BlogCategory category, BlogCategoryUpdateRequest req) {
        if (req.name() != null) {
            category.setName(req.name());
        }
        if (req.description() != null) {
            category.setDescription(req.description());
        }
        if (req.slug() != null && !req.slug().isBlank()) {
            updateSlug(category, req.slug());
        }
    }

    private void updateSlug(BlogCategory category, String newSlug) {
        String slug = SlugUtil.toSlug(newSlug);
        validateSlug(slug);
        ensureSlugNotInUse(slug, category.getSlug());
        category.setSlug(slug);
    }

    private void ensureSlugNotInUse(String newSlug, String currentSlug) {
        if (!newSlug.equalsIgnoreCase(currentSlug) && categoryRepository.existsBySlug(newSlug)) {
            throw new ResourceAlreadyExistsException("Slug already exists");
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        if (postRepository.existsByCategoriesId(id)) {
            throw new CannotDeleteException("Cannot delete category with associated posts");
        }
        categoryRepository.deleteAllByIdInBatch(List.of(id));
    }

    @Override
    public BlogCategoryResponse get(UUID id) {
        BlogCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return blogMapper.toResponse(category);
    }

    @Override
    public PaginationResponse list(Pageable pageable) {
        Page<BlogCategoryResponse> page = categoryRepository.findAll(pageable).map(blogMapper::toResponse);
        return PaginationResponse.from(page, pageable);
    }
}
