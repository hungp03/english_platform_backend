package com.english.api.admin.controller;

import com.english.api.admin.dto.response.*;
import com.english.api.admin.service.AdminOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/overview")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOverviewController {

    private final AdminOverviewService adminOverviewService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        return ResponseEntity.ok(adminOverviewService.getDashboardSummary());
    }

    @GetMapping("/pending-actions")
    public ResponseEntity<PendingActionsResponse> getPendingActions() {
        return ResponseEntity.ok(adminOverviewService.getPendingActions());
    }

    @GetMapping("/charts/user-growth")
    public ResponseEntity<UserGrowthResponse> getUserGrowthChart(@RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(adminOverviewService.getUserGrowthChart(months));
    }

    @GetMapping("/charts/revenue")
    public ResponseEntity<RevenueChartResponse> getRevenueChart(@RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(adminOverviewService.getRevenueChart(months));
    }

    @GetMapping("/charts/enrollments")
    public ResponseEntity<EnrollmentChartResponse> getEnrollmentChart(@RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(adminOverviewService.getEnrollmentChart(months));
    }

    @GetMapping("/top-courses")
    public ResponseEntity<TopPerformersResponse> getTopCourses(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminOverviewService.getTopCourses(limit));
    }

    @GetMapping("/top-instructors")
    public ResponseEntity<TopPerformersResponse> getTopInstructors(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminOverviewService.getTopInstructors(limit));
    }

    @GetMapping("/top-revenue-courses")
    public ResponseEntity<TopPerformersResponse> getTopRevenueCourses(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminOverviewService.getTopRevenueCourses(limit));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportDashboardData(@RequestParam(defaultValue = "summary") String type) {
        // TODO: Implement export
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=dashboard_" + type + ".csv")
                .body("exported".getBytes());
    }
}