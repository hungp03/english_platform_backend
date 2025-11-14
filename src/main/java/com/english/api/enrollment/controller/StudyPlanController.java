package com.english.api.enrollment.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.enrollment.dto.request.AIStudyPlanRequest;
import com.english.api.enrollment.dto.request.CreateStudyPlanRequest;
import com.english.api.enrollment.dto.request.CreateStudyPlanScheduleRequest;
import com.english.api.enrollment.dto.request.UpdateStudyPlanRequest;
import com.english.api.enrollment.dto.request.UpdateStudyPlanScheduleRequest;
import com.english.api.enrollment.dto.response.StudyPlanDetailResponse;
import com.english.api.enrollment.dto.response.StudyPlanResponse;
import com.english.api.enrollment.dto.response.StudyPlanScheduleResponse;
import com.english.api.enrollment.service.StudyPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
public class StudyPlanController {
    private final StudyPlanService studyPlanService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginationResponse> getMyStudyPlans(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(studyPlanService.getMyStudyPlans(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudyPlanDetailResponse> getStudyPlanById(@PathVariable UUID id) {
        return ResponseEntity.ok(studyPlanService.getStudyPlanById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudyPlanResponse> createStudyPlan(@Valid @RequestBody CreateStudyPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studyPlanService.createStudyPlan(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudyPlanDetailResponse
    > updateStudyPlan(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStudyPlanRequest request
    ) {
        return ResponseEntity.ok(studyPlanService.updateStudyPlan(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteStudyPlan(@PathVariable UUID id) {
        studyPlanService.deleteStudyPlan(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{planId}/schedules")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudyPlanScheduleResponse> addSchedule(
            @PathVariable UUID planId,
            @Valid @RequestBody CreateStudyPlanScheduleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studyPlanService.addSchedule(planId, request));
    }

    @PutMapping("/{planId}/schedules/{scheduleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudyPlanDetailResponse> updateSchedule(
            @PathVariable UUID planId,
            @PathVariable UUID scheduleId,
            @Valid @RequestBody UpdateStudyPlanScheduleRequest request
    ) {
        return ResponseEntity.ok(studyPlanService.updateSchedule(planId, scheduleId, request));
    }

    @DeleteMapping("/{planId}/schedules/{scheduleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable UUID planId,
            @PathVariable UUID scheduleId
    ) {
        studyPlanService.deleteSchedule(planId, scheduleId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{planId}/schedules/{scheduleId}/mark-complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markScheduleComplete(
            @PathVariable UUID planId,
            @PathVariable UUID scheduleId
    ) {
        studyPlanService.markScheduleComplete(planId, scheduleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate-ai-plan")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> generateAIPlan(@Valid @RequestBody AIStudyPlanRequest request) {
        return ResponseEntity.ok(studyPlanService.generateAIPlan(request));
    }
}