package com.english.api.forum.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumReportCreateRequest;
import com.english.api.forum.dto.response.ForumReportResponse;
import com.english.api.forum.model.ReportTargetType;
import com.english.api.forum.service.ForumReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/forum")
public class ForumReportController {

    private final ForumReportService reportService;

    // Report a thread
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/threads/{id}/reports")
    public ResponseEntity<ForumReportResponse> reportThread(@PathVariable UUID id,
                                                            @RequestBody ForumReportCreateRequest body) {
        ForumReportCreateRequest req = new ForumReportCreateRequest(ReportTargetType.THREAD, id, body.reason());
        return ResponseEntity.ok(reportService.create(req));
    }

    // Report a post
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/posts/{id}/reports")
    public ResponseEntity<ForumReportResponse> reportPost(@PathVariable UUID id,
                                                          @RequestBody ForumReportCreateRequest body) {
        ForumReportCreateRequest req = new ForumReportCreateRequest(ReportTargetType.POST, id, body.reason());
        return ResponseEntity.ok(reportService.create(req));
    }

    // Create a generic report
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reports")
    public ResponseEntity<ForumReportResponse> createReport(@RequestBody ForumReportCreateRequest req) {
        return ResponseEntity.ok(reportService.create(req));
    }

    // Admin: Get all reports with filters
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports")
    public ResponseEntity<PaginationResponse> getReports(
            @RequestParam(required = false) ReportTargetType type,
            @RequestParam(defaultValue = "true") boolean onlyOpen,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        ReportTargetType t = (type == null) ? ReportTargetType.THREAD : type;
        return ResponseEntity.ok(reportService.list(t, onlyOpen, pageable));
    }

    // Admin: Resolve a report
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reports/{id}/resolve")
    public ResponseEntity<ForumReportResponse> resolveReport(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.resolve(id));
    }
}
