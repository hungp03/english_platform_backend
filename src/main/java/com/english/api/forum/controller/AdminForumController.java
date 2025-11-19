package com.english.api.forum.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumCategoryCreateRequest;
import com.english.api.forum.dto.request.ForumCategoryUpdateRequest;
import com.english.api.forum.dto.response.ForumCategoryResponse;
import com.english.api.forum.dto.response.ForumPostResponse;
import com.english.api.forum.dto.response.ForumReportResponse;
import com.english.api.forum.dto.response.ForumThreadResponse;
import com.english.api.forum.entity.ReportTargetType;
import com.english.api.forum.service.ForumCategoryService;
import com.english.api.forum.service.ForumPostService;
import com.english.api.forum.service.ForumReportService;
import com.english.api.forum.service.ForumThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/forum")
@PreAuthorize("hasRole('ADMIN')")
public class AdminForumController {

    private final ForumCategoryService categoryService;
    private final ForumThreadService threadService;
    private final ForumPostService postService;
    private final ForumReportService reportService;

    @GetMapping("/categories")
    public ResponseEntity<java.util.List<ForumCategoryResponse>> categories() {
        return ResponseEntity.ok(categoryService.list());
    }

    @PostMapping("/categories")
    public ResponseEntity<ForumCategoryResponse> createCategory(@RequestBody ForumCategoryCreateRequest req) {
        return ResponseEntity.ok(categoryService.create(req));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ForumCategoryResponse> updateCategory(@PathVariable UUID id,
                                                                @RequestBody ForumCategoryUpdateRequest req) {
        return ResponseEntity.ok(categoryService.update(id, req));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCat(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/threads")
    public ResponseEntity<PaginationResponse> listThreads(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean locked,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        return ResponseEntity.ok(threadService.listPublic(keyword, categoryId, locked, pageable));
    }

    @PatchMapping("/threads/{id}/lock")
    public ResponseEntity<ForumThreadResponse> lock(@PathVariable UUID id) {
        return ResponseEntity.ok(threadService.adminLock(id, true));
    }

    @PatchMapping("/threads/{id}/unlock")
    public ResponseEntity<ForumThreadResponse> unlock(@PathVariable UUID id) {
        return ResponseEntity.ok(threadService.adminLock(id, false));
    }

    @GetMapping("/reports")
    public ResponseEntity<PaginationResponse> reports(
            @RequestParam(required = false) ReportTargetType type,
            @RequestParam(defaultValue = "true") boolean onlyOpen,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        var t = (type == null) ? ReportTargetType.THREAD : type;
        return ResponseEntity.ok(reportService.list(t, onlyOpen, pageable));
    }

    @PostMapping("/reports/{id}/resolve")
    public ResponseEntity<ForumReportResponse> resolve(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.resolve(id));
    }


    // === Posts moderation ===
    @PostMapping("/posts/{id}/hide")
    public ResponseEntity<ForumPostResponse> hide(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(postService.hide(id));
    }

    @PostMapping("/posts/{id}/unhide")
    public ResponseEntity<ForumPostResponse> unhide(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(postService.show(id));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable java.util.UUID id) {
        postService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }
}
