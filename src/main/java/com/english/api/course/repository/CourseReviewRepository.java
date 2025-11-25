package com.english.api.course.repository;

import com.english.api.course.model.CourseReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, UUID> {
    
    /**
     * Check if user has already reviewed a course
     */
    boolean existsByCourseIdAndUserId(UUID courseId, UUID userId);
    
    /**
     * Find review by course and user
     */
    Optional<CourseReview> findByCourseIdAndUserId(UUID courseId, UUID userId);
    
    /**
     * Get all published reviews for a course (with eager loading)
     */
    @Query("""
        SELECT r FROM CourseReview r
        LEFT JOIN FETCH r.user
        WHERE r.course.id = :courseId 
        AND r.isPublished = true
        ORDER BY r.createdAt DESC
    """)
    Page<CourseReview> findByCourseIdAndIsPublishedTrue(
        @Param("courseId") UUID courseId, 
        Pageable pageable
    );
    
    /**
     * Get all reviews for a course (admin only, includes unpublished)
     */
    @Query("""
        SELECT r FROM CourseReview r
        LEFT JOIN FETCH r.user
        WHERE r.course.id = :courseId
        ORDER BY r.createdAt DESC
    """)
    Page<CourseReview> findByCourseId(
        @Param("courseId") UUID courseId, 
        Pageable pageable
    );
    
    /**
     * Get reviews by user (all courses)
     */
    @Query("""
        SELECT r FROM CourseReview r
        LEFT JOIN FETCH r.course
        WHERE r.user.id = :userId
        ORDER BY r.createdAt DESC
    """)
    Page<CourseReview> findByUserId(
        @Param("userId") UUID userId, 
        Pageable pageable
    );
    
    /**
     * Calculate average rating for a course
     */
    @Query("""
        SELECT AVG(r.rating) 
        FROM CourseReview r 
        WHERE r.course.id = :courseId 
        AND r.isPublished = true
    """)
    Double calculateAverageRating(@Param("courseId") UUID courseId);
    
    /**
     * Count total reviews for a course
     */
    @Query("""
        SELECT COUNT(r) 
        FROM CourseReview r 
        WHERE r.course.id = :courseId 
        AND r.isPublished = true
    """)
    Long countByCourseId(@Param("courseId") UUID courseId);
    
    /**
     * Count reviews by rating for a course
     */
    @Query("""
        SELECT COUNT(r) 
        FROM CourseReview r 
        WHERE r.course.id = :courseId 
        AND r.rating = :rating 
        AND r.isPublished = true
    """)
    Long countByCourseIdAndRating(
        @Param("courseId") UUID courseId, 
        @Param("rating") Integer rating
    );
    
    /**
     * Delete all reviews for a course
     */
    void deleteByCourseId(UUID courseId);
    
    /**
     * Delete all reviews by a user
     */
    void deleteByUserId(UUID userId);
}
