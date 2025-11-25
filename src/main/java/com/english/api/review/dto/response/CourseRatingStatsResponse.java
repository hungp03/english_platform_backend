package com.english.api.review.dto.response;

/**
 * Response DTO for course rating statistics
 * Contains aggregated rating data for a course
 */
public record CourseRatingStatsResponse(
    Long totalReviews,
    Double averageRating,
    Long fiveStarCount,
    Long fourStarCount,
    Long threeStarCount,
    Long twoStarCount,
    Long oneStarCount
) {
    
    /**
     * Calculate percentage for each star rating
     */
    public RatingDistribution getRatingDistribution() {
        if (totalReviews == 0) {
            return new RatingDistribution(0.0, 0.0, 0.0, 0.0, 0.0);
        }
        
        return new RatingDistribution(
            (fiveStarCount * 100.0) / totalReviews,
            (fourStarCount * 100.0) / totalReviews,
            (threeStarCount * 100.0) / totalReviews,
            (twoStarCount * 100.0) / totalReviews,
            (oneStarCount * 100.0) / totalReviews
        );
    }
    
    public record RatingDistribution(
        Double fiveStarPercentage,
        Double fourStarPercentage,
        Double threeStarPercentage,
        Double twoStarPercentage,
        Double oneStarPercentage
    ) {}
}
