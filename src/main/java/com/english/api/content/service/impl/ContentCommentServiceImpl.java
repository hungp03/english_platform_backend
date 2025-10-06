package com.english.api.content.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.content.dto.request.CommentCreateRequest;
import com.english.api.content.dto.request.CommentUpdateRequest;
import com.english.api.content.dto.response.CommentResponse;
import com.english.api.content.model.ContentComment;
import com.english.api.content.model.ContentPost;
import com.english.api.content.repository.ContentCommentRepository;
import com.english.api.content.repository.ContentPostRepository;
import com.english.api.content.service.ContentCommentService;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentCommentServiceImpl implements ContentCommentService {

    private final ContentCommentRepository commentRepository;
    private final ContentPostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponse create(UUID postId, CommentCreateRequest req) {
        ContentPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        ContentComment parent = null;
        if (req.getParentId() != null) {
            parent = commentRepository.findById(req.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
        }

        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User author = userRepository.findById(currentUserId).orElse(null);

        ContentComment c = ContentComment.builder()
                .post(post)
                .parent(parent)
                .author(author)
                .bodyMd(req.getBodyMd())
                .published(true) // default true; moderators may hide later
                .build();
        c = commentRepository.save(c);
        return toResponse(c);
    }

    @Override
    @Transactional
    public CommentResponse update(UUID commentId, CommentUpdateRequest req) {
        ContentComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        c.setBodyMd(req.getBodyMd());
        c = commentRepository.save(c);
        return toResponse(c);
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
        ContentComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        c.setPublished(false);
        c = commentRepository.save(c);
        return toResponse(c);
    }

    @Override
    @Transactional
    public CommentResponse unhide(UUID commentId) {
        ContentComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        c.setPublished(true);
        c = commentRepository.save(c);
        return toResponse(c);
    }

    @Override
    public Page<CommentResponse> listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
        ContentPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (includeUnpublished) {
            return commentRepository.findByPost(post, pageable).map(this::toResponse);
        } else {
            return commentRepository.findByPostAndPublishedIsTrue(post, pageable).map(this::toResponse);
        }
    }

    private CommentResponse toResponse(ContentComment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .postId(c.getPost().getId())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .authorId(c.getAuthor() != null ? c.getAuthor().getId() : null)
                .bodyMd(c.getBodyMd())
                .published(c.isPublished())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}