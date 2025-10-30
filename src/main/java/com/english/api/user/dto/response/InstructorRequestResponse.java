package com.english.api.user.dto.response;

import com.english.api.user.model.InstructorRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for InstructorRequest
 * Created by hungpham on 10/29/2025
 */
public record InstructorRequestResponse(
        UUID id,
        UUID userId,
        String fullName,
        String email,
        String bio,
        String expertise,
        Integer experienceYears,
        String qualification,
        String reason,
        String adminNotes,
        InstructorRequest.Status status,
        Instant requestedAt,
        Instant reviewedAt,
        UUID reviewedBy,
        String reviewedByName,
        List<CertificateProofResponse> certificateProofs
) {
}
