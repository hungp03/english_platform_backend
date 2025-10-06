package com.english.api.content.service;

import com.english.api.content.dto.request.CommentCreateRequest;
import com.english.api.content.dto.request.CommentUpdateRequest;
import com.english.api.content.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ContentCommentService {
    CommentResponse create(UUID postId, CommentCreateRequest req);
    CommentResponse update(UUID commentId, CommentUpdateRequest req);
    void delete(UUID commentId);
    CommentResponse hide(UUID commentId);
    CommentResponse unhide(UUID commentId);
    Page<CommentResponse> listByPost(UUID postId, Pageable pageable, boolean includeUnpublished);
}