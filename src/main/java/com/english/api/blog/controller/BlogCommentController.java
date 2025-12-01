package com.english.api.blog.controller;

import com.english.api.blog.dto.request.CommentCreateRequest;
import com.english.api.blog.dto.response.CommentResponse;
import com.english.api.blog.service.BlogCommentService;
import com.english.api.common.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/blog/comments")
@RequiredArgsConstructor
public class BlogCommentController {

    private final BlogCommentService commentService;

    // ============ PUBLIC ENDPOINTS ============

    @GetMapping("/post/{postId}")
    public ResponseEntity<PaginationResponse> listByPost(@PathVariable UUID postId, Pageable pageable) {
        return ResponseEntity.ok(commentService.listByPost(postId, pageable, false));
    }

    // ============ AUTHENTICATED USER ENDPOINTS ============

    @PostMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> create(@PathVariable UUID postId,
                                                  @Valid @RequestBody CommentCreateRequest req) {
        return ResponseEntity.ok(commentService.create(postId, req));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}
