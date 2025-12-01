package com.english.api.forum.controller;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumThreadCreateRequest;
import com.english.api.forum.dto.request.ForumThreadUpdateRequest;
import com.english.api.forum.dto.response.ForumThreadResponse;
import com.english.api.forum.service.ForumThreadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/forum/threads")
public class ForumThreadController {

    private final ForumThreadService threadService;

    // Get all forum threads with filters
    @GetMapping
    public ResponseEntity<PaginationResponse> getThreads(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean locked,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, sort);
        return ResponseEntity.ok(threadService.listPublic(keyword, categoryId, locked, pageable));
    }

    // Get thread by slug
    @GetMapping("/{slug}")
    public ResponseEntity<ForumThreadResponse> getThreadBySlug(@PathVariable String slug) {
        ForumThreadResponse forumThreadResponse = threadService.getBySlug(slug);
        threadService.increaseView(forumThreadResponse.id());
        return ResponseEntity.ok(forumThreadResponse);
    }

    // Create a new thread
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ForumThreadResponse> createThread(@Valid @RequestBody ForumThreadCreateRequest req) {
        return ResponseEntity.ok(threadService.create(req));
    }

    // Update own thread
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<ForumThreadResponse> updateThread(
            @PathVariable UUID id,
            @RequestBody ForumThreadUpdateRequest req) {
        return ResponseEntity.ok(threadService.updateByOwner(id, req));
    }

    // Delete own thread
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteThread(@PathVariable UUID id) {
        threadService.deleteByOwner(id);
        return ResponseEntity.noContent().build();
    }

    // Get threads created by current user
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<PaginationResponse> getMyThreads(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageRequest pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        UUID uid = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(threadService.listByAuthor(uid, pageable));
    }

    // Lock own thread
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/lock")
    public ResponseEntity<ForumThreadResponse> lockThread(@PathVariable UUID id) {
        return ResponseEntity.ok(threadService.lockByOwner(id, true));
    }

    // Unlock own thread
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/unlock")
    public ResponseEntity<ForumThreadResponse> unlockThread(@PathVariable UUID id) {
        return ResponseEntity.ok(threadService.lockByOwner(id, false));
    }

    // Admin: Lock any thread
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/admin/lock")
    public ResponseEntity<ForumThreadResponse> adminLockThread(@PathVariable UUID id) {
        return ResponseEntity.ok(threadService.adminLock(id, true));
    }

    // Admin: Unlock any thread
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/admin/unlock")
    public ResponseEntity<ForumThreadResponse> adminUnlockThread(@PathVariable UUID id) {
        return ResponseEntity.ok(threadService.adminLock(id, false));
    }

    // Admin: Delete any thread
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/admin")
    public ResponseEntity<Void> adminDeleteThread(@PathVariable UUID id) {
        threadService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }
}
