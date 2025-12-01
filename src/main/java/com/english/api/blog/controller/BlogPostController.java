package com.english.api.blog.controller;

import com.english.api.blog.dto.request.PostCreateRequest;
import com.english.api.blog.dto.request.PostFilterRequest;
import com.english.api.blog.dto.request.PostUpdateRequest;
import com.english.api.blog.dto.response.PostResponse;
import com.english.api.blog.dto.response.PublicPostDetailResponse;
import com.english.api.blog.service.BlogPostService;
import com.english.api.common.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/blog/posts")
@RequiredArgsConstructor
public class BlogPostController {

    private final BlogPostService postService;

    // ============ PUBLIC ENDPOINTS ============

    @GetMapping
    public ResponseEntity<PaginationResponse> listPublished(@ModelAttribute PostFilterRequest filter,
                                                            Pageable pageable) {
        return ResponseEntity.ok(postService.publicList(filter, pageable));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PublicPostDetailResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(postService.publicDetailBySlug(slug));
    }

    // ============ AUTHENTICATED USER ENDPOINTS ============

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest req) {
        return ResponseEntity.ok(postService.create(req, false));
    }

    // ============ ADMIN ENDPOINTS ============

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse> searchAll(@ModelAttribute PostFilterRequest filter,
                                                        Pageable pageable) {
        return ResponseEntity.ok(postService.search(filter, pageable, true));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.get(id));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostResponse> adminCreatePost(@Valid @RequestBody PostCreateRequest req) {
        return ResponseEntity.ok(postService.create(req, true));
    }

    @PatchMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostResponse> update(@PathVariable UUID id,
                                               @RequestBody PostUpdateRequest req) {
        return ResponseEntity.ok(postService.update(id, req));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostResponse> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.publish(id));
    }

    @PostMapping("/admin/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostResponse> unpublish(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.unpublish(id));
    }
}
