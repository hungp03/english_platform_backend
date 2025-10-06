package com.english.api.content.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.content.dto.request.PostCreateRequest;
import com.english.api.content.dto.request.PostFilterRequest;
import com.english.api.content.dto.request.PostUpdateRequest;
import com.english.api.content.dto.response.CategoryResponse;
import com.english.api.content.dto.response.PostResponse;
import com.english.api.content.dto.response.PublicPostDetailResponse;
import com.english.api.content.dto.response.PublicPostSummaryResponse;
import com.english.api.content.model.ContentCategory;
import com.english.api.content.model.ContentPost;
import com.english.api.content.repository.ContentCategoryRepository;
import com.english.api.content.repository.ContentPostRepository;
import com.english.api.content.service.ContentPostService;
import com.english.api.content.spec.PostSpecifications;
import com.english.api.content.util.SlugUtil;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentPostServiceImpl implements ContentPostService {

    private final ContentPostRepository postRepository;
    private final ContentCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PostResponse create(PostCreateRequest req, boolean canPublish) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User author = userRepository.findById(currentUserId)
                .orElse(null); // nullable aligns with schema

        String slug = (req.getSlug() != null && !req.getSlug().isBlank())
                ? SlugUtil.toSlug(req.getSlug())
                : SlugUtil.toSlug(req.getTitle());
        if (!SlugUtil.isSeoFriendly(slug)) throw new IllegalArgumentException("Slug is not SEO-friendly");
        slug = ensureUniqueSlug(slug, null);

        Set<ContentCategory> categories = resolveCategories(req.getCategoryIds());

        ContentPost post = ContentPost.builder()
                .author(author)
                .title(req.getTitle())
                .slug(slug)
                .bodyMd(req.getBodyMd())
                .published(false)
                .categories(categories)
                .build();

        if (canPublish) {
            post.setPublished(true);
            post.setPublishedAt(Instant.now());
        }

        post = postRepository.save(post);
        return toResponse(post);
    }

    @Override
    @Transactional
    public PostResponse update(UUID id, PostUpdateRequest req) {
        ContentPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (req.getTitle() != null) post.setTitle(req.getTitle());
        if (req.getBodyMd() != null) post.setBodyMd(req.getBodyMd());
        if (req.getSlug() != null && !req.getSlug().isBlank()) {
            String s = SlugUtil.toSlug(req.getSlug());
            if (!SlugUtil.isSeoFriendly(s)) throw new IllegalArgumentException("Slug is not SEO-friendly");
            s = ensureUniqueSlug(s, id);
            post.setSlug(s);
        }
        if (req.getCategoryIds() != null) {
            post.setCategories(resolveCategories(req.getCategoryIds()));
        }
        post = postRepository.save(post);
        return toResponse(post);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!postRepository.existsById(id)) throw new ResourceNotFoundException("Post not found");
        postRepository.deleteById(id);
    }

    @Override
    public PostResponse get(UUID id) {
        ContentPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return toResponse(post);
    }

    @Override
    public Page<PostResponse> search(PostFilterRequest filter, Pageable pageable, boolean includeUnpublished) {
        Specification<ContentPost> spec = Specification.where(PostSpecifications.publishedOnly(!includeUnpublished))
                .and(PostSpecifications.keyword(filter.getKeyword()))
                .and(PostSpecifications.author(filter.getAuthorId()))
                .and(PostSpecifications.categoryId(filter.getCategoryId()))
                .and(PostSpecifications.categorySlug(filter.getCategorySlug()))
                .and(PostSpecifications.dateRange(filter.getFromDate(), filter.getToDate()));
        return postRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public PostResponse publish(UUID id) {
        ContentPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        post.setPublished(true);
        if (post.getPublishedAt() == null) post.setPublishedAt(Instant.now());
        post = postRepository.save(post);
        return toResponse(post);
    }

    @Override
    @Transactional
    public PostResponse unpublish(UUID id) {
        ContentPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        post.setPublished(false);
        post = postRepository.save(post);
        return toResponse(post);
    }

    // ------- public -------
    @Override
    public Page<PublicPostSummaryResponse> publicList(PostFilterRequest filter, Pageable pageable) {
        Specification<ContentPost> spec = Specification.where(PostSpecifications.publishedOnly(true))
                .and(PostSpecifications.keyword(filter.getKeyword()))
                .and(PostSpecifications.author(filter.getAuthorId()))
                .and(PostSpecifications.categoryId(filter.getCategoryId()))
                .and(PostSpecifications.categorySlug(filter.getCategorySlug()))
                .and(PostSpecifications.dateRange(filter.getFromDate(), filter.getToDate()));
        return postRepository.findAll(spec, pageable).map(this::toPublicSummary);
    }

    @Override
    public PublicPostDetailResponse publicDetailBySlug(String slug) {
        ContentPost post = postRepository.findBySlugAndPublishedIsTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return toPublicDetail(post);
    }

    // ------- helpers -------
    private Set<ContentCategory> resolveCategories(List<java.util.UUID> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return ids.stream()
                .map(id -> categoryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id)))
                .collect(Collectors.toSet());
    }

    private String ensureUniqueSlug(String base, UUID excludeId) {
        String slug = base;
        int suffix = 1;
        while (true) {
            Optional<ContentPost> existing = postRepository.findBySlug(slug);
            if (existing.isEmpty() || (excludeId != null && existing.get().getId().equals(excludeId))) {
                return slug;
            }
            suffix++;
            slug = base + "-" + suffix;
        }
    }

    private CategoryResponse toCategoryResp(ContentCategory c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private PostResponse toResponse(ContentPost p) {
        return PostResponse.builder()
                .id(p.getId())
                .authorId(p.getAuthor() != null ? p.getAuthor().getId() : null)
                .title(p.getTitle())
                .slug(p.getSlug())
                .bodyMd(p.getBodyMd())
                .published(p.isPublished())
                .publishedAt(p.getPublishedAt())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .categories(p.getCategories().stream().map(this::toCategoryResp).toList())
                .build();
    }

    private PublicPostSummaryResponse toPublicSummary(ContentPost p) {
        return PublicPostSummaryResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .slug(p.getSlug())
                .published(p.isPublished())
                .publishedAt(p.getPublishedAt())
                .createdAt(p.getCreatedAt())
                .categories(p.getCategories().stream().map(this::toCategoryResp).toList())
                .build();
    }

    private PublicPostDetailResponse toPublicDetail(ContentPost p) {
        return PublicPostDetailResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .slug(p.getSlug())
                .bodyMd(p.getBodyMd())
                .publishedAt(p.getPublishedAt())
                .createdAt(p.getCreatedAt())
                .authorId(p.getAuthor() != null ? p.getAuthor().getId() : null)
                .categories(p.getCategories().stream().map(this::toCategoryResp).toList())
                .build();
    }
}