package com.english.api.user.controller;

import com.english.api.user.dto.request.CreateInstructorRequest;
import com.english.api.user.dto.request.ReviewInstructorRequest;
import com.english.api.user.dto.request.UpdateInstructorRequest;
import com.english.api.user.dto.request.UploadCertificateProofRequest;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.user.dto.response.CertificateProofResponse;
import com.english.api.user.dto.response.InstructorRequestResponse;
import com.english.api.user.model.InstructorRequest;
import com.english.api.user.service.InstructorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Instructor Request operations
 * Created by hungpham on 10/29/2025
 */
@RestController
@RequestMapping("/api/instructor-requests")
@RequiredArgsConstructor
@Slf4j
public class InstructorController {

    private final InstructorService instructorService;

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
    public ResponseEntity<CertificateProofResponse> uploadCertificateProof(
            @PathVariable UUID requestId,
            @Valid @RequestBody UploadCertificateProofRequest request) {
        CertificateProofResponse response = instructorService.uploadCertificateProof(requestId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse> getAllInstructorRequests(
            @RequestParam(required = false) InstructorRequest.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("DESC") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

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

    @DeleteMapping("/admin/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable UUID requestId) {
        instructorService.deleteRequest(requestId);
        return ResponseEntity.noContent().build();
    }
}
