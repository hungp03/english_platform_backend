package com.english.api.review.mapper;

import com.english.api.review.dto.response.ReviewResponse;
import com.english.api.review.dto.response.ReviewSummaryResponse;
import com.english.api.review.model.CourseReview;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    
    /**
     * Convert entity to full response DTO
     */
    public ReviewResponse toResponse(CourseReview review) {
        if (review == null) {
            return null;
        }
        
        return new ReviewResponse(
            review.getId(),
            review.getCourse() != null ? review.getCourse().getId() : null,
            review.getCourse() != null ? review.getCourse().getTitle() : null,
            review.getUser() != null ? review.getUser().getId() : null,
            review.getUser() != null ? review.getUser().getFullName() : null,
            review.getUser() != null ? review.getUser().getAvatarUrl() : null,
            review.getRating(),
            review.getComment(),
            review.getIsPublished(),
            review.getCreatedAt(),
            review.getUpdatedAt()
        );
    }
    
    /**
     * Convert entity to summary response DTO
     */
    public ReviewSummaryResponse toSummaryResponse(CourseReview review) {
        if (review == null) {
            return null;
        }
        
        return new ReviewSummaryResponse(
            review.getId(),
            review.getUser() != null ? review.getUser().getId() : null,
            review.getUser() != null ? review.getUser().getFullName() : null,
            review.getUser() != null ? review.getUser().getAvatarUrl() : null,
            review.getRating(),
            review.getComment(),
            review.getCreatedAt()
        );
    }
}
