package com.english.api.content.controller.publicapi;

import com.english.api.content.dto.request.CommentCreateRequest;
import com.english.api.content.dto.request.PostCreateRequest;
import com.english.api.content.dto.response.CommentResponse;
import com.english.api.content.dto.response.PostResponse;
import com.english.api.content.service.ContentCommentService;
import com.english.api.content.service.ContentPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/app/content")
@RequiredArgsConstructor
public class AppContentController {

    private final ContentPostService postService;
    private final ContentCommentService commentService;

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