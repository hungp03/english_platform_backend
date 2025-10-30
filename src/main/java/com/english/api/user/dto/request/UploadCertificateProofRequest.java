package com.english.api.user.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for uploading certificate proof
 * Created by hungpham on 10/30/2025
 */
public record UploadCertificateProofRequest(
        @NotBlank(message = "File URL is required")
        String fileUrl
) {
}
