package com.english.api.user.controller;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.course.dto.response.InstructorStatsResponse;
import com.english.api.course.dto.response.PublicInstructorStatsResponse;
import com.english.api.course.service.CourseService;
import com.english.api.user.dto.request.CreateInstructorRequest;
import com.english.api.user.dto.request.ReviewInstructorRequest;
import com.english.api.user.dto.request.RevokeInstructorRoleRequest;
import com.english.api.user.dto.request.UpdateInstructorRequest;
import com.english.api.user.dto.request.UploadCertificateProofRequest;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.user.dto.response.CertificateProofResponse;
import com.english.api.user.dto.response.InstructorProfileResponse;
import com.english.api.user.dto.response.InstructorRequestResponse;
import com.english.api.user.dto.response.PublicInstructorResponse;
import com.english.api.user.model.InstructorProfile;
import com.english.api.user.model.InstructorRequest;
import com.english.api.user.repository.InstructorProfileRepository;
import com.english.api.user.service.InstructorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
@Slf4j
public class InstructorController {

    private final InstructorService instructorService;
    private final CourseService courseService;
    private final InstructorProfileRepository instructorProfileRepository;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<InstructorRequestResponse> createInstructorRequest(
            @Valid @RequestBody CreateInstructorRequest request) {
        InstructorRequestResponse response = instructorService.createInstructorRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<InstructorRequestResponse> getCurrentUserRequest() {
        InstructorRequestResponse response = instructorService.getUserCurrentRequest();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/{requestId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<InstructorRequestResponse> getUserRequestById(
            @PathVariable UUID requestId) {
        InstructorRequestResponse response = instructorService.getUserRequestById(requestId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<InstructorRequestResponse>> getUserRequests() {
        List<InstructorRequestResponse> response = instructorService.getUserRequests();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{requestId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<InstructorRequestResponse> updatePendingRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody UpdateInstructorRequest request) {
        InstructorRequestResponse response = instructorService.updatePendingRequest(requestId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{requestId}/certificate-proofs")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CertificateProofResponse>> uploadCertificateProof(
            @PathVariable UUID requestId,
            @Valid @RequestBody UploadCertificateProofRequest request) {
        List<CertificateProofResponse> responses = instructorService.uploadCertificateProof(requestId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/{requestId}/certificate-proofs")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CertificateProofResponse>> getCertificateProofs(
            @PathVariable UUID requestId) {
        List<CertificateProofResponse> response = instructorService.getCertificateProofs(requestId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{requestId}/certificate-proofs/{proofId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteCertificateProof(
            @PathVariable UUID requestId,
            @PathVariable UUID proofId) {
        instructorService.deleteCertificateProof(requestId, proofId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/certificate-proofs/{proofId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteCertificateProofByOwner(
            @PathVariable UUID proofId) {
        instructorService.deleteCertificateProofByOwner(proofId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse> getAllInstructorRequests(
            @RequestParam(required = false) InstructorRequest.Status status,
            @PageableDefault(size = 10, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PaginationResponse response = instructorService.getAllInstructorRequests(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstructorRequestResponse> getInstructorRequest(
            @PathVariable UUID requestId) {

        InstructorRequestResponse response = instructorService.getInstructorRequest(requestId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/admin/{requestId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstructorRequestResponse> reviewInstructorRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody ReviewInstructorRequest reviewRequest) {
        InstructorRequestResponse response = instructorService.reviewInstructorRequest(
                requestId, reviewRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{requestId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable UUID requestId) {
        instructorService.deleteRequest(requestId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get comprehensive statistics for the current instructor
     * Returns: total courses, published courses, total students, and total revenue
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<InstructorStatsResponse> getMyStats() {
        UUID instructorId = SecurityUtil.getCurrentUserId();
        InstructorStatsResponse stats = courseService.getInstructorStats(instructorId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get statistics for a specific instructor (admin only)
     */
    @GetMapping("/admin/{instructorId}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstructorStatsResponse> getInstructorStats(
            @PathVariable UUID instructorId) {
        InstructorStatsResponse stats = courseService.getInstructorStats(instructorId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get list of all instructors with basic information
     * Supports search by email or fullName
     */
    @GetMapping("/list-instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse> getAllInstructors(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PaginationResponse instructors = instructorService.getAllInstructors(search, pageable);
        return ResponseEntity.ok(instructors);
    }

    /**
     * Manage INSTRUCTOR role - revoke or restore permissions (admin only)
     */
    @PatchMapping("/admin/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> manageInstructorRole(
            @PathVariable UUID userId,
            @Valid @RequestBody RevokeInstructorRoleRequest request) {
        instructorService.manageInstructorRole(userId, request.action(), request.reason());
        return ResponseEntity.noContent().build();
    }

    // ==================== Public Endpoints ====================

    // Get public instructor profile and stats
    @GetMapping("/{userId}")
    public ResponseEntity<PublicInstructorResponse> getPublicInstructorProfile(
            @PathVariable UUID userId) {
        InstructorProfile profile = instructorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor profile not found"));

        InstructorProfileResponse profileDto = new InstructorProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getFullName(),
                profile.getUser().getEmail(),
                profile.getUser().getAvatarUrl(),
                profile.getBio(),
                profile.getExpertise(),
                profile.getExperienceYears(),
                profile.getQualification(),
                profile.getCreatedAt(),
                profile.getUpdatedAt());

        InstructorStatsResponse fullStats = courseService.getInstructorStats(userId);
        PublicInstructorStatsResponse publicStats = new PublicInstructorStatsResponse(
                fullStats.totalCourses(),
                fullStats.publishedCourses(),
                fullStats.totalStudents()
        );
        return ResponseEntity.ok(new PublicInstructorResponse(profileDto, publicStats));
    }

    // Get published courses by instructor
    @GetMapping("/{userId}/courses")
    public ResponseEntity<PaginationResponse> getInstructorPublishedCourses(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String[] skills) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(courseService.getPublishedByInstructor(userId, pageable, keyword, skills));
    }
}
