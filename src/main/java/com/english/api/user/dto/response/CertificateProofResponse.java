package com.english.api.user.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for InstructorCertificateProof
 * Created by hungpham on 10/30/2025
 */
public record CertificateProofResponse(
        UUID id,
        String fileUrl,
        Instant uploadedAt
) {
}
