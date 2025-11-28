package com.english.api.forum.controller;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumPostCreateRequest;
import com.english.api.forum.dto.request.ForumReportCreateRequest;
import com.english.api.forum.dto.request.ForumThreadCreateRequest;
import com.english.api.forum.dto.request.ForumThreadUpdateRequest;
import com.english.api.forum.dto.response.ForumPostResponse;
import com.english.api.forum.dto.response.ForumReportResponse;
import com.english.api.forum.dto.response.ForumThreadResponse;
import com.english.api.forum.entity.ReportTargetType;
import com.english.api.forum.service.ForumPostService;
import com.english.api.forum.service.ForumReportService;
import com.english.api.forum.service.ForumThreadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/forum")
public class AppForumController {

    private final ForumThreadService threadService;
    private final ForumPostService postService;
    private final ForumReportService reportService;

    @PostMapping("/threads")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumThreadResponse> createThread(@Valid @RequestBody ForumThreadCreateRequest req) {
        return ResponseEntity.ok(threadService.create(req));
    }

    @PatchMapping("/threads/{id}/lock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumThreadResponse> lockOwnThread(@PathVariable UUID id) {
        return ResponseEntity.ok(threadService.lockByOwner(id, true));
    }

    @PatchMapping("/threads/{id}/unlock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumThreadResponse> unlockOwnThread(@PathVariable UUID id) {
        return ResponseEntity.ok(threadService.lockByOwner(id, false));
    }

    @PostMapping("/threads/{threadId}/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumPostResponse> reply(@PathVariable UUID threadId,
                                                   @Valid @RequestBody ForumPostCreateRequest req) {
        return ResponseEntity.ok(postService.create(threadId, req));
    }

    @PostMapping("/threads/{id}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumReportResponse> reportThread(@PathVariable UUID id,
                                                            @RequestBody ForumReportCreateRequest body) {
        ForumReportCreateRequest req = new ForumReportCreateRequest(ReportTargetType.THREAD, id, body.reason());
        return ResponseEntity.ok(reportService.create(req));
    }

    @PostMapping("/posts/{id}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumReportResponse> reportPost(@PathVariable UUID id,
                                                          @RequestBody ForumReportCreateRequest body) {
        ForumReportCreateRequest req = new ForumReportCreateRequest(ReportTargetType.POST, id, body.reason());
        return ResponseEntity.ok(reportService.create(req));
    }

    @PostMapping("/reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumReportResponse> report(@RequestBody ForumReportCreateRequest req) {
        return ResponseEntity.ok(reportService.create(req));
    }


    // === Owner self-delete post ===
    @DeleteMapping("/posts/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteOwnPost(@PathVariable UUID id) {
        postService.deleteByOwner(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/threads")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginationResponse> myThreads(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        PageRequest pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        UUID uid = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(threadService.listByAuthor(uid, pageable));
    }

    @PutMapping("/threads/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumThreadResponse> updateMyThread(
            @PathVariable UUID id,
            @RequestBody ForumThreadUpdateRequest req) {
        return ResponseEntity.ok(threadService.updateByOwner(id, req));
    }

    @DeleteMapping("/threads/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMyThread(@PathVariable UUID id) {
        threadService.deleteByOwner(id);
        return ResponseEntity.noContent().build();
    }
}
