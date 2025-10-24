package com.english.api.order.repository;

import com.english.api.order.model.Order;
import com.english.api.order.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Repository interface for Order entity with custom query methods
 * Provides data access operations for orders with filtering and pagination support
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Find orders by user ID with pagination support
     * @param userId the user ID to filter by
     * @param pageable pagination parameters
     * @return page of orders for the specified user
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find orders by status with pagination support
     * @param status the order status to filter by
     * @param pageable pagination parameters
     * @return page of orders with the specified status
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    /**
     * Find orders by user ID and status with pagination support
     * @param userId the user ID to filter by
     * @param status the order status to filter by
     * @param pageable pagination parameters
     * @return page of orders matching both user and status criteria
     */
    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, OrderStatus status, Pageable pageable);

    /**
     * Find orders created within a date range with pagination support
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return page of orders created within the specified date range
     */
    @Query("""
        SELECT o FROM Order o 
        WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate 
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findByCreatedAtBetween(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);

    /**
     * Find orders by user ID within a date range with pagination support
     * @param userId the user ID to filter by
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return page of orders matching user and date criteria
     */
    @Query("""
        SELECT o FROM Order o 
        WHERE o.user.id = :userId 
        AND o.createdAt >= :startDate AND o.createdAt <= :endDate 
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findByUserIdAndCreatedAtBetween(
            @Param("userId") UUID userId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);

    /**
     * Find orders by status within a date range with pagination support
     * @param status the order status to filter by
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return page of orders matching status and date criteria
     */
    @Query("""
        SELECT o FROM Order o 
        WHERE o.status = :status 
        AND o.createdAt >= :startDate AND o.createdAt <= :endDate 
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findByStatusAndCreatedAtBetween(
            @Param("status") OrderStatus status,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);

    /**
     * Find orders by user ID, status, and date range with pagination support
     * @param userId the user ID to filter by
     * @param status the order status to filter by
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return page of orders matching all criteria
     */
    @Query("""
        SELECT o FROM Order o 
        WHERE o.user.id = :userId AND o.status = :status 
        AND o.createdAt >= :startDate AND o.createdAt <= :endDate 
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findByUserIdAndStatusAndCreatedAtBetween(
            @Param("userId") UUID userId,
            @Param("status") OrderStatus status,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);

    /**
     * Find orders containing items for courses created by a specific instructor
     * This method supports instructor revenue analytics by finding orders for their courses
     * @param instructorId the instructor's user ID
     * @param pageable pagination parameters
     * @return page of orders containing items for the instructor's courses
     */
    @Query("""
        SELECT DISTINCT o FROM Order o 
        JOIN o.items oi 
        JOIN Course c ON c.id = oi.entityId 
        WHERE oi.entity = 'COURSE' AND c.createdBy.id = :instructorId 
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findOrdersForInstructorCourses(@Param("instructorId") UUID instructorId, Pageable pageable);

    /**
     * Find orders containing items for courses created by a specific instructor within a date range
     * @param instructorId the instructor's user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return page of orders containing items for the instructor's courses within the date range
     */
    @Query("""
        SELECT DISTINCT o FROM Order o 
        JOIN o.items oi 
        JOIN Course c ON c.id = oi.entityId 
        WHERE oi.entity = 'COURSE' AND c.createdBy.id = :instructorId 
        AND o.createdAt >= :startDate AND o.createdAt <= :endDate 
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findOrdersForInstructorCoursesInDateRange(
            @Param("instructorId") UUID instructorId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);

    /**
     * Find orders containing items for courses created by a specific instructor with specific status
     * @param instructorId the instructor's user ID
     * @param status the order status to filter by
     * @param pageable pagination parameters
     * @return page of orders containing items for the instructor's courses with the specified status
     */
    @Query("""
        SELECT DISTINCT o FROM Order o 
        JOIN o.items oi 
        JOIN Course c ON c.id = oi.entityId 
        WHERE oi.entity = 'COURSE' AND c.createdBy.id = :instructorId AND o.status = :status 
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findOrdersForInstructorCoursesByStatus(
            @Param("instructorId") UUID instructorId,
            @Param("status") OrderStatus status,
            Pageable pageable);

    /**
     * Find orders containing items for a specific course
     * @param courseId the course ID to filter by
     * @param pageable pagination parameters
     * @return page of orders containing items for the specified course
     */
    @Query("""
        SELECT DISTINCT o FROM Order o 
        JOIN o.items oi 
        WHERE oi.entity = 'COURSE' AND oi.entityId = :courseId 
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findOrdersForCourse(@Param("courseId") UUID courseId, Pageable pageable);

    /**
     * Find orders containing items for a specific course within a date range
     * @param courseId the course ID to filter by
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return page of orders containing items for the specified course within the date range
     */
    @Query("""
        SELECT DISTINCT o FROM Order o 
        JOIN o.items oi 
        WHERE oi.entity = 'COURSE' AND oi.entityId = :courseId 
        AND o.createdAt >= :startDate AND o.createdAt <= :endDate 
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findOrdersForCourseInDateRange(
            @Param("courseId") UUID courseId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);

    /**
     * Count total orders for a specific instructor's courses
     * @param instructorId the instructor's user ID
     * @return total count of orders for the instructor's courses
     */
    @Query("""
        SELECT COUNT(DISTINCT o.id) FROM Order o 
        JOIN o.items oi 
        JOIN Course c ON c.id = oi.entityId 
        WHERE oi.entity = 'COURSE' AND c.createdBy.id = :instructorId
        """)
    Long countOrdersForInstructorCourses(@Param("instructorId") UUID instructorId);

    /**
     * Count total orders for a specific instructor's courses within a date range
     * @param instructorId the instructor's user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return total count of orders for the instructor's courses within the date range
     */
    @Query("""
        SELECT COUNT(DISTINCT o.id) FROM Order o 
        JOIN o.items oi 
        JOIN Course c ON c.id = oi.entityId 
        WHERE oi.entity = 'COURSE' AND c.createdBy.id = :instructorId 
        AND o.createdAt >= :startDate AND o.createdAt <= :endDate
        """)
    Long countOrdersForInstructorCoursesInDateRange(
            @Param("instructorId") UUID instructorId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);

    /**
     * Find all orders with eager loading of items for efficient data access
     * @param pageable pagination parameters
     * @return page of orders with items loaded
     */
    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.items
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findAllWithItems(Pageable pageable);

    /**
     * Find orders by user with eager loading of items and payments
     * @param userId the user ID to filter by
     * @param pageable pagination parameters
     * @return page of orders with items and payments loaded
     */
    @Query("""
        SELECT DISTINCT o FROM Order o 
        LEFT JOIN FETCH o.items 
        LEFT JOIN FETCH o.payments 
        WHERE o.user.id = :userId 
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findByUserIdWithItemsAndPayments(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Check if user has already purchased a specific course
     * @return true if user has a paid order containing this course, false otherwise
     */
    @Query("""
        SELECT COUNT(o) > 0 FROM Order o 
        JOIN o.items oi 
        WHERE o.user.id = :userId 
        AND o.status = 'PAID' 
        AND oi.entity = 'COURSE' 
        AND oi.entityId = :courseId
        """)
    boolean hasUserPurchasedCourse(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    /**
     * Get all course IDs that user has already purchased (batch check)
     * @param userId the user ID to check
     * @param courseIds the list of course IDs to check
     * @return set of course IDs that user has purchased
     */
    @Query("""
        SELECT DISTINCT oi.entityId FROM Order o 
        JOIN o.items oi 
        WHERE o.user.id = :userId 
        AND o.status = 'PAID' 
        AND oi.entity = 'COURSE' 
        AND oi.entityId IN :courseIds
        """)
    Set<UUID> findPurchasedCourseIds(@Param("userId") UUID userId, @Param("courseIds") List<UUID> courseIds);
}