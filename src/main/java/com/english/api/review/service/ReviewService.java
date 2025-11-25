package com.english.api.review.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.review.dto.request.CreateReviewRequest;
import com.english.api.review.dto.request.UpdateReviewRequest;
import com.english.api.review.dto.response.CourseRatingStatsResponse;
import com.english.api.review.dto.response.ReviewResponse;

import java.util.UUID;

public interface ReviewService {
    
    /**
     * Create a new review for a course
     * User must be enrolled in the course
     * 
     * @param courseId Course to review
     * @param request Review data
     * @return Created review
     * @throws com.english.api.common.exception.ResourceNotFoundException if course not found
     * @throws com.english.api.common.exception.ResourceAlreadyExistsException if user already reviewed
     * @throws com.english.api.common.exception.OperationNotAllowedException if user not enrolled
     */
    ReviewResponse createReview(UUID courseId, CreateReviewRequest request);
    
    /**
     * Update an existing review
     * Only the review author can update
     * 
     * @param reviewId Review to update
     * @param request Updated data
     * @return Updated review
     * @throws com.english.api.common.exception.ResourceNotFoundException if review not found
     * @throws com.english.api.common.exception.AccessDeniedException if not the author
     */
    ReviewResponse updateReview(UUID reviewId, UpdateReviewRequest request);
    
    /**
     * Delete a review
     * Only the review author can delete
     * 
     * @param reviewId Review to delete
     * @throws com.english.api.common.exception.ResourceNotFoundException if review not found
     * @throws com.english.api.common.exception.AccessDeniedException if not the author
     */
    void deleteReview(UUID reviewId);
    
    /**
     * Get a specific review by ID
     * 
     * @param reviewId Review ID
     * @return Review details
     */
    ReviewResponse getReviewById(UUID reviewId);
    
    /**
     * Get current user's review for a course
     * 
     * @param courseId Course ID
     * @return Review if exists, null otherwise
     */
    ReviewResponse getMyReviewForCourse(UUID courseId);
    
    /**
     * Get all published reviews for a course (public)
     * 
     * @param courseId Course ID
     * @param page Page number
     * @param size Page size
     * @return Paginated reviews
     */
    PaginationResponse getReviewsForCourse(UUID courseId, int page, int size);
    
    /**
     * Get all reviews for a course including unpublished (admin only)
     * 
     * @param courseId Course ID
     * @param page Page number
     * @param size Page size
     * @return Paginated reviews
     */
    PaginationResponse getAllReviewsForCourse(UUID courseId, int page, int size);
    
    /**
     * Get all reviews by current user
     * 
     * @param page Page number
     * @param size Page size
     * @return Paginated reviews
     */
    PaginationResponse getMyReviews(int page, int size);
    
    /**
     * Get rating statistics for a course
     * 
     * @param courseId Course ID
     * @return Rating stats (average, count by stars, etc.)
     */
    CourseRatingStatsResponse getCourseRatingStats(UUID courseId);
    
    /**
     * Hide a review (admin only)
     * 
     * @param reviewId Review to hide
     * @return Updated review
     */
    ReviewResponse hideReview(UUID reviewId);
    
    /**
     * Show a review (admin only)
     * 
     * @param reviewId Review to show
     * @return Updated review
     */
    ReviewResponse showReview(UUID reviewId);
}
