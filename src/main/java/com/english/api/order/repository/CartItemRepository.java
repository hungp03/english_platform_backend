package com.english.api.order.repository;

import com.english.api.order.model.CartItem;
import com.english.api.order.dto.response.CartCheckoutResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    /**
     * Find all cart items for a specific user with pagination
     */
    @Query("""
        SELECT ci FROM CartItem ci
        JOIN FETCH ci.course c
        LEFT JOIN FETCH c.createdBy
        WHERE ci.user.id = :userId
        AND c.status = 'PUBLISHED'
    """)
    Page<CartItem> findByUserIdWithPublishedCourses(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find all cart items for a specific user (no pagination)
     */
    @Query("""
        SELECT ci FROM CartItem ci
        JOIN FETCH ci.course c
        LEFT JOIN FETCH c.createdBy
        WHERE ci.user.id = :userId
        AND c.status = 'PUBLISHED'
        ORDER BY ci.addedAt DESC
    """)
    List<CartItem> findAllByUserIdWithPublishedCourses(@Param("userId") UUID userId);

    /**
     * Count published courses in user's cart
     */
    @Query("""
        SELECT COUNT(ci)
        FROM CartItem ci
        JOIN ci.course c
        WHERE ci.user.id = :userId
        AND c.status = 'PUBLISHED'
    """)
    long countPublishedByUserId(@Param("userId") UUID userId);

    /**
     * Calculate total price of published courses in user's cart
     */
    @Query("""
        SELECT COALESCE(SUM(c.priceCents), 0)
        FROM CartItem ci
        JOIN ci.course c
        WHERE ci.user.id = :userId
        AND c.status = 'PUBLISHED'
    """)
    Long sumTotalPriceByUserId(@Param("userId") UUID userId);

    /**
     * Check if a course is already in user's cart
     */
    @Query("""
        SELECT CASE WHEN COUNT(ci) > 0 THEN true ELSE false END
        FROM CartItem ci
        WHERE ci.user.id = :userId
        AND ci.course.id = :courseId
    """)
    boolean existsByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    /**
     * Delete a cart item by user and course
     */
    @Modifying
    @Query("""
        DELETE FROM CartItem ci
        WHERE ci.user.id = :userId
        AND ci.course.id = :courseId
    """)
    void deleteByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    /**
     * Delete multiple cart items by user and course IDs (batch delete)
     */
    @Modifying
    @Query("""
        DELETE FROM CartItem ci
        WHERE ci.user.id = :userId
        AND ci.course.id IN :courseIds
    """)
    void deleteByUserIdAndCourseIdIn(@Param("userId") UUID userId, @Param("courseIds") List<UUID> courseIds);

    /**
     * Delete all cart items for a user
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    /**
     * Count cart items for a user
     */
    long countByUserId(UUID userId);

    /**
     * Get all courses in user's cart for checkout (only essential fields)
     * Excludes courses that user has already purchased
     */
    @Query("""
        SELECT new com.english.api.order.dto.response.CartCheckoutResponse(
            c.id,
            c.title,
            c.thumbnail,
            c.priceCents,
            c.currency
        )
        FROM CartItem ci
        JOIN ci.course c
        WHERE ci.user.id = :userId
        AND c.status = 'PUBLISHED'
        AND NOT EXISTS (
            SELECT 1 FROM Order o
            JOIN o.items oi
            WHERE o.user.id = :userId
            AND o.status = 'PAID'
            AND oi.entity = 'COURSE'
            AND oi.entityId = c.id
        )
    """)
    List<CartCheckoutResponse> findCoursesForCheckoutByUserId(@Param("userId") UUID userId);
}
