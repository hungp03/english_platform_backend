package com.english.api.forum.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumPostCreateRequest;
import com.english.api.forum.dto.response.ForumPostResponse;
import com.english.api.forum.service.ForumPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/forum")
public class ForumPostController {

    private final ForumPostService postService;

    // Get all posts for a thread
    @GetMapping("/threads/{threadId}/posts")
    public ResponseEntity<PaginationResponse> getThreadPosts(
            @PathVariable UUID threadId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        return ResponseEntity.ok(postService.listByThread(threadId, pageable, true));
    }

    // Create a reply/post in a thread
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/threads/{threadId}/posts")
    public ResponseEntity<ForumPostResponse> createPost(@PathVariable UUID threadId,
                                                        @Valid @RequestBody ForumPostCreateRequest req) {
        return ResponseEntity.ok(postService.create(threadId, req));
    }

    // Delete own post
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deleteByOwner(id);
        return ResponseEntity.noContent().build();
    }

    // Admin: Hide a post
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/posts/{id}/hide")
    public ResponseEntity<ForumPostResponse> hidePost(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.hide(id));
    }

    // Admin: Unhide a post
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/posts/{id}/unhide")
    public ResponseEntity<ForumPostResponse> unhidePost(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.show(id));
    }

    // Admin: Delete any post
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/posts/{id}/admin")
    public ResponseEntity<Void> adminDeletePost(@PathVariable UUID id) {
        postService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }
}
