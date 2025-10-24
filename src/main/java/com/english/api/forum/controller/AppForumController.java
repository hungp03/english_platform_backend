package com.english.api.forum.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumPostCreateRequest;
import com.english.api.forum.dto.request.ForumReportCreateRequest;
import com.english.api.forum.dto.request.ForumThreadCreateRequest;
import com.english.api.forum.dto.response.ForumPostResponse;
import com.english.api.forum.dto.response.ForumReportResponse;
import com.english.api.forum.dto.response.ForumThreadResponse;
import com.english.api.forum.entity.ReportTargetType;
import com.english.api.forum.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    var req = new ForumReportCreateRequest(ReportTargetType.THREAD, id, body.reason());
    return ResponseEntity.ok(reportService.create(req));
  }

  @PostMapping("/posts/{id}/report")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ForumReportResponse> reportPost(@PathVariable UUID id,
                                                        @RequestBody ForumReportCreateRequest body) {
    var req = new ForumReportCreateRequest(ReportTargetType.POST, id, body.reason());
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
  public ResponseEntity<Void> deleteOwnPost(@PathVariable java.util.UUID id) {
    postService.deleteByOwner(id);
    return ResponseEntity.noContent().build();
  }
}
