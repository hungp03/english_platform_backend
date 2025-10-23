package com.english.api.user.controller;

import com.english.api.auth.security.CustomUserDetails;
import com.english.api.user.dto.request.CreateInstructorRequest;
import com.english.api.user.dto.request.ReviewInstructorRequest;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.user.dto.response.InstructorRequestListResponse;
import com.english.api.user.dto.response.InstructorRequestResponse;
import com.english.api.user.model.InstructorRequest;
import com.english.api.user.service.InstructorRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/instructor-requests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Instructor Request Management", description = "APIs for managing instructor registration requests")
@SecurityRequirement(name = "bearerAuth")
public class InstructorRequestController {

    private final InstructorRequestService instructorRequestService;

    @PostMapping
    @Operation(summary = "Submit instructor registration request", description = "Users can submit a request to become an instructor")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<InstructorRequestResponse> createInstructorRequest(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateInstructorRequest request) {

        InstructorRequestResponse response = instructorRequestService.createInstructorRequest(currentUser.user().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's instructor request", description = "Get the current user's active instructor request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<InstructorRequestResponse> getCurrentUserRequest(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        InstructorRequestResponse response = instructorRequestService.getUserCurrentRequest(currentUser.user().getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/can-submit")
    @Operation(summary = "Check if user can submit request", description = "Check if the current user can submit a new instructor request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> canUserSubmitRequest(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        boolean canSubmit = instructorRequestService.canUserSubmitRequest(currentUser.user().getId());
        return ResponseEntity.ok(canSubmit);
    }

    @GetMapping("/admin")
    @Operation(summary = "Get all instructor requests (Admin)", description = "Get paginated list of all instructor requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse> getAllInstructorRequests(
            @Parameter(description = "Filter by request status (PENDING, APPROVED, REJECTED)")
            @RequestParam(required = false) InstructorRequest.Status status,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "requestedAt") String sortBy,

            @Parameter(description = "Sort direction (ASC, DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("DESC") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PaginationResponse response = instructorRequestService.getAllInstructorRequests(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/{requestId}")
    @Operation(summary = "Get specific instructor request (Admin)", description = "Get detailed information about a specific instructor request")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstructorRequestResponse> getInstructorRequest(
            @Parameter(description = "Request ID")
            @PathVariable UUID requestId) {

        InstructorRequestResponse response = instructorRequestService.getInstructorRequest(requestId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/admin/{requestId}/review")
    @Operation(summary = "Review instructor request (Admin)", description = "Approve or reject an instructor request")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstructorRequestResponse> reviewInstructorRequest(
            @Parameter(description = "Request ID")
            @PathVariable UUID requestId,

            @Valid @RequestBody ReviewInstructorRequest reviewRequest,

            @AuthenticationPrincipal CustomUserDetails currentUser) {

        InstructorRequestResponse response = instructorRequestService.reviewInstructorRequest(
                requestId, reviewRequest, currentUser.user().getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/{requestId}")
    @Operation(summary = "Delete instructor request (Admin)", description = "Hard delete an instructor request")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRequest(
            @Parameter(description = "Request ID")
            @PathVariable UUID requestId) {

        instructorRequestService.deleteRequest(requestId);
        return ResponseEntity.noContent().build();
    }
}