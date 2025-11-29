package com.english.api.course.mapper;

import com.english.api.course.dto.response.ReviewResponse;
import com.english.api.course.dto.response.ReviewSummaryResponse;
import com.english.api.course.model.CourseReview;
import org.springframework.stereotype.Component;
import com.english.api.course.dto.response.MyReviewResponse;;
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

    public MyReviewResponse toMyReviewResponse(CourseReview review) {
        if (review == null) {
            return null;
        }
        
        return new MyReviewResponse(
            review.getId(),
            review.getCourse() != null ? review.getCourse().getId() : null,
            review.getCourse() != null ? review.getCourse().getTitle() : null,
            review.getCourse() != null ? review.getCourse().getSlug() : null,
            review.getCourse() != null ? review.getCourse().getThumbnail() : null,
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
