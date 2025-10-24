package com.english.api.content.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
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
import com.english.api.content.repository.ContentCommentRepository;
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
    private final ContentCommentRepository commentRepository;

    @Override
    @Transactional
    public PostResponse create(PostCreateRequest req, boolean canPublish) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User author = userRepository.findById(currentUserId).orElse(null);

        String slug = (req.slug() != null && !req.slug().isBlank())
                ? SlugUtil.toSlug(req.slug())
                : SlugUtil.toSlug(req.title());
        if (!SlugUtil.isSeoFriendly(slug)) throw new IllegalArgumentException("Slug is not SEO-friendly");
        slug = ensureUniqueSlug(slug, null);

        Set<ContentCategory> categories = resolveCategories(req.categoryIds());

        ContentPost post = ContentPost.builder()
                .author(author)
                .title(req.title())
                .slug(slug)
                .bodyMd(req.bodyMd())
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

        if (req.title() != null) post.setTitle(req.title());
        if (req.bodyMd() != null) post.setBodyMd(req.bodyMd());
        if (req.slug() != null && !req.slug().isBlank()) {
            String s = SlugUtil.toSlug(req.slug());
            if (!SlugUtil.isSeoFriendly(s)) throw new IllegalArgumentException("Slug is not SEO-friendly");
            s = ensureUniqueSlug(s, id);
            post.setSlug(s);
        }
        if (req.categoryIds() != null) {
            post.setCategories(resolveCategories(req.categoryIds()));
        }
        post = postRepository.save(post);
        return toResponse(post);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!postRepository.existsById(id)) throw new ResourceNotFoundException("Post not found");
        // Xóa toàn bộ comment của post trước
        commentRepository.deleteByPostId(id);
        // Sau đó xóa post
        postRepository.deleteById(id);
    }

    @Override
    public PostResponse get(UUID id) {
        ContentPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return toResponse(post);
    }

    @Override
    public PaginationResponse search(PostFilterRequest filter, Pageable pageable, boolean includeUnpublished) {
        Specification<ContentPost> spec = Specification.where(PostSpecifications.publishedOnly(!includeUnpublished))
                .and(PostSpecifications.keyword(filter.keyword()))
                .and(PostSpecifications.author(filter.authorId()))
                .and(PostSpecifications.categoryId(filter.categoryId()))
                .and(PostSpecifications.categorySlug(filter.categorySlug()))
                .and(PostSpecifications.dateRange(filter.fromDate(), filter.toDate()));

        Page<PostResponse> page = postRepository.findAll(spec, pageable).map(this::toResponse);
        return PaginationResponse.from(page, pageable); // meta.page = pageable.pageNumber + 1
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
    public PaginationResponse publicList(PostFilterRequest filter, Pageable pageable) {
        Specification<ContentPost> spec = Specification.where(PostSpecifications.publishedOnly(true))
                .and(PostSpecifications.keyword(filter.keyword()))
                .and(PostSpecifications.author(filter.authorId()))
                .and(PostSpecifications.categoryId(filter.categoryId()))
                .and(PostSpecifications.categorySlug(filter.categorySlug()))
                .and(PostSpecifications.dateRange(filter.fromDate(), filter.toDate()));

        Page<PublicPostSummaryResponse> page =
                postRepository.findAll(spec, pageable).map(this::toPublicSummary);
        return PaginationResponse.from(page, pageable);
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
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getCreatedAt());
    }

    private PostResponse toResponse(ContentPost p) {
        return new PostResponse(
                p.getId(),
                p.getAuthor() != null ? p.getAuthor().getId() : null,
                p.getTitle(),
                p.getSlug(),
                p.getBodyMd(),
                p.isPublished(),
                p.getPublishedAt(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                p.getCategories().stream().map(this::toCategoryResp).toList()
        );
    }

    private PublicPostSummaryResponse toPublicSummary(ContentPost p) {
        return new PublicPostSummaryResponse(
                p.getId(), p.getTitle(), p.getSlug(), p.isPublished(), p.getPublishedAt(), p.getCreatedAt(),
                p.getCategories().stream().map(this::toCategoryResp).toList()
        );
    }

    private PublicPostDetailResponse toPublicDetail(ContentPost p) {
        return new PublicPostDetailResponse(
                p.getId(), p.getTitle(), p.getSlug(), p.getBodyMd(), p.getPublishedAt(), p.getCreatedAt(),
                p.getAuthor() != null ? p.getAuthor().getId() : null,
                p.getCategories().stream().map(this::toCategoryResp).toList()
        );
    }
}
