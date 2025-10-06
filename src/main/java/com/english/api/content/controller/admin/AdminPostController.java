package com.english.api.content.controller.admin;

import com.english.api.content.dto.request.PostCreateRequest;
import com.english.api.content.dto.request.PostFilterRequest;
import com.english.api.content.dto.request.PostUpdateRequest;
import com.english.api.content.dto.response.PostResponse;
import com.english.api.content.service.ContentPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/content/posts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
public class AdminPostController {

    private final ContentPostService service;

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
    public ResponseEntity<Page<PostResponse>> search(PostFilterRequest filter, Pageable pageable) {
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