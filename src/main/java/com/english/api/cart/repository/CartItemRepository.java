package com.english.api.cart.repository;

import com.english.api.cart.model.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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
        AND c.published = true
    """)
    Page<CartItem> findByUserIdWithPublishedCourses(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Count published courses in user's cart
     */
    @Query("""
        SELECT COUNT(ci)
        FROM CartItem ci
        JOIN ci.course c
        WHERE ci.user.id = :userId
        AND c.published = true
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
        AND c.published = true
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
     * Find a specific cart item by user and course
     */
    Optional<CartItem> findByUserIdAndCourseId(UUID userId, UUID courseId);

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
     * Delete all cart items for a user
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    /**
     * Count cart items for a user
     */
    long countByUserId(UUID userId);
}
