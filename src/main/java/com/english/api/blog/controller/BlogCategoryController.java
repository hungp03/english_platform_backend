package com.english.api.blog.controller;

import com.english.api.blog.dto.request.BlogCategoryCreateRequest;
import com.english.api.blog.dto.request.BlogCategoryUpdateRequest;
import com.english.api.blog.dto.response.BlogCategoryResponse;
import com.english.api.blog.service.BlogCategoryService;
import com.english.api.common.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/blog/categories")
@RequiredArgsConstructor
public class BlogCategoryController {

    private final BlogCategoryService service;

    // ============ PUBLIC ENDPOINTS ============

    @GetMapping
    public ResponseEntity<PaginationResponse> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    // ============ ADMIN ENDPOINTS ============

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BlogCategoryResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BlogCategoryResponse> create(@Valid @RequestBody BlogCategoryCreateRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BlogCategoryResponse> update(@PathVariable UUID id,
                                                        @Valid @RequestBody BlogCategoryUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
