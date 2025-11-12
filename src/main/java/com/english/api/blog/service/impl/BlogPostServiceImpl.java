package com.english.api.blog.service.impl;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.blog.dto.request.PostCreateRequest;
import com.english.api.blog.dto.request.PostFilterRequest;
import com.english.api.blog.dto.request.PostUpdateRequest;
import com.english.api.blog.dto.response.PostResponse;
import com.english.api.blog.dto.response.PublicPostDetailResponse;
import com.english.api.blog.dto.response.PublicPostSummaryResponse;
import com.english.api.blog.mapper.BlogMapper;
import com.english.api.blog.model.BlogCategory;
import com.english.api.blog.model.BlogPost;
import com.english.api.blog.repository.BlogCategoryRepository;
import com.english.api.blog.repository.BlogCommentRepository;
import com.english.api.blog.repository.BlogPostRepository;
import com.english.api.blog.service.BlogPostService;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.common.util.SlugUtil;
import com.english.api.user.model.User;
import com.english.api.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogPostServiceImpl implements BlogPostService{
    private final UserService userService;
    private final BlogPostRepository postRepository;
    private final BlogCategoryRepository categoryRepository;
    private final BlogCommentRepository commentRepository;
    private final BlogMapper blogPostMapper;

    @Override
    @Transactional
    public PostResponse create(PostCreateRequest req, boolean canPublish) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User author = userService.findById(currentUserId);

        String slug = SlugUtil.toSlugWithUuid(req.title());

        Set<BlogCategory> categories = resolveCategories(req.categoryIds());

        BlogPost post = BlogPost.builder()
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
        return blogPostMapper.toResponse(post);
    }

    @Override
    @Transactional
    public PostResponse update(UUID id, PostUpdateRequest req) {
        BlogPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (req.title() != null) {
            post.setTitle(req.title());
            post.setSlug(SlugUtil.toSlugWithUuid(req.title()));
        }
        if (req.bodyMd() != null) {
            post.setBodyMd(req.bodyMd());
        }
        if (req.categoryIds() != null) {
            post.setCategories(resolveCategories(req.categoryIds()));
        }
        
        post = postRepository.save(post);
        return blogPostMapper.toResponse(post);
    }

    @Override
    public void delete(UUID id) {
        if (!postRepository.existsById(id)) throw new ResourceNotFoundException("Post not found");
        // Xóa toàn bộ comment của post trước
        commentRepository.deleteByPostId(id);
        // Sau đó xóa post
        postRepository.deleteById(id);
    }

    @Override
    public PostResponse get(UUID id) {
        BlogPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return blogPostMapper.toResponse(post);
    }

    @Override
    public PaginationResponse search(PostFilterRequest filter, Pageable pageable, boolean includeUnpublished) {
        String processedKeyword = filter.keyword() != null 
            ? "%" + filter.keyword().toLowerCase() + "%" 
            : null;
            
        // First query: get paginated post IDs
        Page<BlogPost> postsPage = postRepository.searchPosts(
            processedKeyword,
            filter.authorId(),
            filter.categoryId(),
            filter.categorySlug(),
            filter.fromDate(),
            filter.toDate(),
            includeUnpublished,
            pageable
        );
        
        // If no results, return empty page
        if (postsPage.isEmpty()) {
            return PaginationResponse.from(postsPage.map(blogPostMapper::toResponse), pageable);
        }
        
        // Second query: fetch full entities with associations
        List<UUID> postIds = postsPage.getContent().stream()
            .map(BlogPost::getId)
            .toList();
        
        List<BlogPost> postsWithAssociations = postRepository.findByIdInWithAssociations(postIds);
        
        // Preserve the original order from pagination
        java.util.Map<UUID, BlogPost> postMap = postsWithAssociations.stream()
            .collect(Collectors.toMap(BlogPost::getId, p -> p));
        
        List<PostResponse> responses = postIds.stream()
            .map(postMap::get)
            .map(blogPostMapper::toResponse)
            .toList();
        
        Page<PostResponse> page = new org.springframework.data.domain.PageImpl<>(
            responses, pageable, postsPage.getTotalElements()
        );
        
        return PaginationResponse.from(page, pageable);
    }

    @Override
    @Transactional
    public PostResponse publish(UUID id) {
        BlogPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        
        if (!post.isPublished()) {
            post.setPublished(true);
            post.setPublishedAt(Instant.now());
            post = postRepository.save(post);
        }
        
        return blogPostMapper.toResponse(post);
    }

    @Override
    @Transactional
    public PostResponse unpublish(UUID id) {
        BlogPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        
        if (post.isPublished()) {
            post.setPublished(false);
            post.setPublishedAt(null);
            post = postRepository.save(post);
        }
        
        return blogPostMapper.toResponse(post);
    }

    @Override
    public PaginationResponse publicList(PostFilterRequest filter, Pageable pageable) {
        String processedKeyword = filter.keyword() != null 
            ? "%" + filter.keyword().toLowerCase() + "%" 
            : null;
            
        // First query: get paginated post IDs
        Page<BlogPost> postsPage = postRepository.searchPosts(
            processedKeyword,
            filter.authorId(),
            filter.categoryId(),
            filter.categorySlug(),
            filter.fromDate(),
            filter.toDate(),
            false,
            pageable
        );
        
        // If no results, return empty page
        if (postsPage.isEmpty()) {
            return PaginationResponse.from(postsPage.map(blogPostMapper::toPublicSummary), pageable);
        }
        
        // Second query: fetch full entities with associations
        List<UUID> postIds = postsPage.getContent().stream()
            .map(BlogPost::getId)
            .toList();
        
        List<BlogPost> postsWithAssociations = postRepository.findByIdInWithAssociations(postIds);
        
        // Preserve the original order from pagination
        java.util.Map<UUID, BlogPost> postMap = postsWithAssociations.stream()
            .collect(Collectors.toMap(BlogPost::getId, p -> p));
        
        List<PublicPostSummaryResponse> responses = postIds.stream()
            .map(postMap::get)
            .map(blogPostMapper::toPublicSummary)
            .toList();
        
        Page<PublicPostSummaryResponse> page = new org.springframework.data.domain.PageImpl<>(
            responses, pageable, postsPage.getTotalElements()
        );
        
        return PaginationResponse.from(page, pageable);
    }

    @Override
    public PublicPostDetailResponse publicDetailBySlug(String slug) {
        BlogPost post = postRepository.findBySlugAndPublishedIsTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return blogPostMapper.toPublicDetail(post);
    }


    // ------- helpers -------
    private Set<BlogCategory> resolveCategories(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        
        List<BlogCategory> categories = categoryRepository.findAllById(ids);
        
        if (categories.size() != ids.size()) {
            Set<UUID> foundIds = categories.stream().map(BlogCategory::getId).collect(Collectors.toSet());
            List<UUID> missingIds = ids.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new ResourceNotFoundException("Categories not found: " + missingIds);
        }
        
        return new HashSet<>(categories);
    }
}


