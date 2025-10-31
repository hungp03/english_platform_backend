package com.english.api.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for uploading certificate proofs
 * Created by hungpham on 10/30/2025
 */
public record UploadCertificateProofRequest(
        @NotEmpty(message = "File URLs list cannot be empty")
        List<@NotBlank(message = "File URL is required") String> fileUrls
) {
}
