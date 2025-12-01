package com.english.api.forum.controller;

import com.english.api.forum.dto.request.ForumCategoryCreateRequest;
import com.english.api.forum.dto.request.ForumCategoryUpdateRequest;
import com.english.api.forum.dto.response.ForumCategoryResponse;
import com.english.api.forum.service.ForumCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/forum/categories")
public class ForumCategoryController {

    private final ForumCategoryService categoryService;

    // Get all forum categories
    @GetMapping
    public ResponseEntity<List<ForumCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.list());
    }

    // Create a new forum category
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ForumCategoryResponse> createCategory(@RequestBody ForumCategoryCreateRequest req) {
        return ResponseEntity.ok(categoryService.create(req));
    }

    // Update a forum category
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ForumCategoryResponse> updateCategory(@PathVariable UUID id,
                                                                @RequestBody ForumCategoryUpdateRequest req) {
        return ResponseEntity.ok(categoryService.update(id, req));
    }

    // Delete a forum category
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
