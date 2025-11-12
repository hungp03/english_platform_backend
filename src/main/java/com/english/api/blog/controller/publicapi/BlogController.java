package com.english.api.blog.controller.publicapi;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.english.api.blog.dto.request.CommentCreateRequest;
import com.english.api.blog.dto.request.PostCreateRequest;
import com.english.api.blog.dto.response.CommentResponse;
import com.english.api.blog.dto.response.PostResponse;
import com.english.api.blog.service.BlogCommentService;
import com.english.api.blog.service.BlogPostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/app/content")
@RequiredArgsConstructor
public class BlogController {
    private final BlogPostService postService;
    private final BlogCommentService commentService;

    // Any authenticated user may create a post (auto-unpublished). Admins can publish from admin APIs.
    @PostMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest req) {
        return ResponseEntity.ok(postService.create(req, false));
    }

    // Any authenticated user may comment on a post
    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> createComment(@PathVariable UUID postId,
                                                         @Valid @RequestBody CommentCreateRequest req) {
        return ResponseEntity.ok(commentService.create(postId, req));
    }
}
