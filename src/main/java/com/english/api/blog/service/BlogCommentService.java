package com.english.api.blog.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.blog.dto.request.CommentCreateRequest;
import com.english.api.blog.dto.request.CommentUpdateRequest;
import com.english.api.blog.dto.response.CommentResponse;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BlogCommentService {
    CommentResponse create(UUID postId, CommentCreateRequest req);
    CommentResponse update(UUID commentId, CommentUpdateRequest req);
    void delete(UUID commentId);
    CommentResponse hide(UUID commentId);
    CommentResponse unhide(UUID commentId);
    PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished);
    PaginationResponse showAllComment(Pageable pageable);
}
