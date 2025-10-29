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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    /**
     * Find orders by status with pagination support
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    /**
     * Find orders created within a date range with pagination support
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
     * Find orders by status within a date range with pagination support
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
     * Find all orders without eager loading (for summary listing)
     */
    @Query("""
        SELECT o FROM Order o
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findAllOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find orders by user without eager loading (for summary listing)
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);


    @Query("""
        SELECT o FROM Order o 
        LEFT JOIN FETCH o.user 
        LEFT JOIN FETCH o.items 
        WHERE o.id = :orderId AND o.user.id = :userId
        """)
    Optional<Order> findByIdAndUserIdWithItems(@Param("orderId") UUID orderId, @Param("userId") UUID userId);

    /**
     * Find order by ID and user ID with eager loading of payments (optimized for user access)
     */
    @Query("""
        SELECT o FROM Order o 
        LEFT JOIN FETCH o.user 
        LEFT JOIN FETCH o.payments 
        WHERE o.id = :orderId AND o.user.id = :userId
        """)
    Optional<Order> findByIdAndUserIdWithPayments(@Param("orderId") UUID orderId, @Param("userId") UUID userId);

    /**
     * Check if user has already purchased a specific course
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