package com.english.api.blog.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.blog.dto.request.CommentCreateRequest;
import com.english.api.blog.dto.request.CommentUpdateRequest;
import com.english.api.blog.dto.response.CommentResponse;
import com.english.api.blog.mapper.BlogMapper;
import com.english.api.blog.model.BlogComment;
import com.english.api.blog.model.BlogPost;
import com.english.api.blog.repository.BlogCommentRepository;
import com.english.api.blog.repository.BlogPostRepository;
import com.english.api.blog.service.BlogCommentService;
import com.english.api.user.model.User;
import com.english.api.user.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlogCommentServiceImpl implements BlogCommentService {

    private final BlogCommentRepository commentRepository;
    private final BlogPostRepository postRepository;
    private final UserService userService;
    private final BlogMapper blogMapper;

    @Override
    @Transactional
    public CommentResponse create(UUID postId, CommentCreateRequest req) {
        BlogPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        BlogComment parent = null;
        if (req.parentId() != null) {
            parent = commentRepository.findById(req.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
        }

        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User author = userService.findById(currentUserId);

        BlogComment c = BlogComment.builder()
                .post(post)
                .parent(parent)
                .author(author)
                .bodyMd(req.bodyMd())
                .published(true)
                .build();
        c = commentRepository.save(c);
        return blogMapper.toCommentResponse(c);
    }

    @Override
    @Transactional
    public CommentResponse update(UUID commentId, CommentUpdateRequest req) {
        BlogComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        c.setBodyMd(req.bodyMd());
        c = commentRepository.save(c);
        return blogMapper.toCommentResponse(c);
    }

    @Override
    @Transactional
    public void delete(UUID commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public CommentResponse hide(UUID commentId) {
        BlogComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        c.setPublished(false);
        c = commentRepository.save(c);
        return blogMapper.toCommentResponse(c);
    }

    @Override
    @Transactional
    public CommentResponse unhide(UUID commentId) {
        BlogComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        c.setPublished(true);
        c = commentRepository.save(c);
        return blogMapper.toCommentResponse(c);
    }

    @Override
    public PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
        BlogPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // First query: get paginated comment IDs
        Page<BlogComment> commentsPage = includeUnpublished
                ? commentRepository.findByPost(post, pageable)
                : commentRepository.findByPostAndPublishedIsTrue(post, pageable);

        // If no results, return empty page
        if (commentsPage.isEmpty()) {
            return PaginationResponse.from(commentsPage.map(blogMapper::toCommentResponse), pageable);
        }

        // Second query: fetch full entities with associations
        List<UUID> commentIds = commentsPage.getContent().stream()
                .map(BlogComment::getId)
                .toList();

        List<BlogComment> commentsWithAssociations = commentRepository.findByIdInWithAssociations(commentIds);

        // Preserve the original order from pagination
        java.util.Map<UUID, BlogComment> commentMap = commentsWithAssociations.stream()
                .collect(java.util.stream.Collectors.toMap(BlogComment::getId, c -> c));

        List<CommentResponse> responses = commentIds.stream()
                .map(commentMap::get)
                .map(blogMapper::toCommentResponse)
                .toList();

        Page<CommentResponse> page = new org.springframework.data.domain.PageImpl<>(
                responses, pageable, commentsPage.getTotalElements()
        );

        return PaginationResponse.from(page, pageable);
    }

    @Override
    public PaginationResponse showAllComment(Pageable pageable) {
        // First query: get paginated comment IDs
        Page<BlogComment> commentsPage = commentRepository.findAll(pageable);

        // If no results, return empty page
        if (commentsPage.isEmpty()) {
            return PaginationResponse.from(commentsPage.map(blogMapper::toCommentResponse), pageable);
        }

        // Second query: fetch full entities with associations
        List<UUID> commentIds = commentsPage.getContent().stream()
                .map(BlogComment::getId)
                .toList();

        List<BlogComment> commentsWithAssociations = commentRepository.findByIdInWithAssociations(commentIds);

        // Preserve the original order from pagination
        java.util.Map<UUID, BlogComment> commentMap = commentsWithAssociations.stream()
                .collect(java.util.stream.Collectors.toMap(BlogComment::getId, c -> c));

        List<CommentResponse> responses = commentIds.stream()
                .map(commentMap::get)
                .map(blogMapper::toCommentResponse)
                .toList();

        Page<CommentResponse> page = new org.springframework.data.domain.PageImpl<>(
                responses, pageable, commentsPage.getTotalElements()
        );

        return PaginationResponse.from(page, pageable);
    }
}
