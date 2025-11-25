package com.english.api.course.dto.request;

import jakarta.validation.constraints.*;

/**
 * Request DTO for updating an existing course review
 */
public record UpdateReviewRequest(
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating must be at most 5 stars")
    Integer rating,
    
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    String comment
    
) {
}
