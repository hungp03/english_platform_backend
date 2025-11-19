package com.english.api.blog.controller.admin;

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
@RequestMapping("/api/admin/content/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {
    private final BlogCategoryService service;

    @PostMapping
    public ResponseEntity<BlogCategoryResponse> create(@Valid @RequestBody BlogCategoryCreateRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BlogCategoryResponse> update(@PathVariable UUID id, @Valid @RequestBody BlogCategoryUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogCategoryResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<PaginationResponse> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }
}
