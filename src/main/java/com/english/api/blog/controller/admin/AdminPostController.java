package com.english.api.blog.controller.admin;

import com.english.api.blog.dto.request.PostCreateRequest;
import com.english.api.blog.dto.request.PostFilterRequest;
import com.english.api.blog.dto.request.PostUpdateRequest;
import com.english.api.blog.dto.response.PostResponse;
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
@RequestMapping("/api/admin/content/posts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPostController {

    private final BlogPostService service;

    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody PostCreateRequest req) {
        // admin/moderator can publish immediately
        return ResponseEntity.ok(service.create(req, true));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PostResponse> update(@PathVariable UUID id, @RequestBody PostUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<PaginationResponse> search(@ModelAttribute PostFilterRequest filter, Pageable pageable) {
        return ResponseEntity.ok(service.search(filter, pageable, true)); // includeUnpublished
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<PostResponse> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(service.publish(id));
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<PostResponse> unpublish(@PathVariable UUID id) {
        return ResponseEntity.ok(service.unpublish(id));
    }
}