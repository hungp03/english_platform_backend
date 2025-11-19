package com.english.api.order.repository;

import com.english.api.order.model.Order;
import com.english.api.order.model.enums.CurrencyType;
import com.english.api.order.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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
        LEFT JOIN FETCH o.items 
        WHERE o.id = :orderId AND o.user.id = :userId
        """)
    Optional<Order> findByIdAndUserIdWithItems(@Param("orderId") UUID orderId, @Param("userId") UUID userId);

    /**
     * Find order by ID and user ID with eager loading of payments (optimized for user access)
     */
    @Query("""
        SELECT o FROM Order o 
        LEFT JOIN FETCH o.payments 
        WHERE o.id = :orderId AND o.user.id = :userId
        """)
    Optional<Order> findByIdAndUserIdWithPayments(@Param("orderId") UUID orderId, @Param("userId") UUID userId);

    /**
     * Find order by ID with eager loading of items (for admin access, no user restriction)
     */
    @Query("""
        SELECT o FROM Order o 
        LEFT JOIN FETCH o.items 
        WHERE o.id = :orderId
        """)
    Optional<Order> findByIdWithItems(@Param("orderId") UUID orderId);

    /**
     * Find order by ID with eager loading of payments (for admin access, no user restriction)
     */
    @Query("""
        SELECT o FROM Order o 
        LEFT JOIN FETCH o.payments 
        WHERE o.id = :orderId
        """)
    Optional<Order> findByIdWithPayments(@Param("orderId") UUID orderId);

    /**
     * Check if user has already purchased a specific course
     * Optimized with EXISTS subquery for better performance
     */
    @Query("""
        SELECT CASE WHEN EXISTS (
            SELECT 1 FROM OrderItem oi 
            WHERE oi.order.user.id = :userId 
            AND oi.order.status = 'PAID' 
            AND oi.entity = 'COURSE' 
            AND oi.entityId = :courseId
        ) THEN true ELSE false END
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

    
    Long countByStatus(OrderStatus status);
    Long countByCreatedAtAfter(OffsetDateTime createdAt);

    @Query("SELECT COALESCE(SUM(o.totalCents), 0) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :start AND :end")
    Long sumTotalCentsByStatusAndCreatedAtBetween(
        @Param("status") OrderStatus status,
        @Param("start") OffsetDateTime start,
        @Param("end") OffsetDateTime end
    );

    @Query("SELECT COALESCE(SUM(o.totalCents), 0) FROM Order o WHERE o.currency = :currency AND o.status = :status")
    Long sumTotalCentsByCurrencyAndStatus(
        @Param("currency") CurrencyType currency,
        @Param("status") OrderStatus status
    );

    @Query("SELECT COALESCE(SUM(o.totalCents), 0) FROM Order o WHERE o.status = :status")
    Long sumTotalCentsByStatus(@Param("status") OrderStatus status);

    List<Order> findTop15ByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(OrderStatus status, OffsetDateTime createdAt);

    @Query("""
        SELECT oi.entityId as courseId, 
               SUM(oi.unitPriceCents * oi.quantity) as totalRevenue, 
               COUNT(DISTINCT o.id) as orderCount
        FROM Order o 
        JOIN o.items oi 
        WHERE o.status = 'PAID' 
          AND oi.entity = 'COURSE'
        GROUP BY oi.entityId 
        ORDER BY totalRevenue DESC
        """)
    List<Object[]> findTopCoursesByRevenue(Pageable pageable);
    
    // Get top instructors by revenue
    // @Query("""
    //     SELECT c.createdBy.id as instructorId, 
    //         SUM(oi.unitPriceCents * oi.quantity) as totalRevenue, 
    //         COUNT(DISTINCT o.id) as orderCount
    //     FROM Order o 
    //     JOIN o.items oi 
    //     JOIN Course c ON oi.entityId = c.id 
    //     WHERE o.status = 'PAID' 
    //     AND oi.entity = 'COURSE'
    //     GROUP BY c.createdBy.id 
    //     ORDER BY totalRevenue DESC
    //     """)
    // List<Object[]> findTopInstructorsByRevenue(Pageable pageable);

    @Query("""
        SELECT 
            u.id, u.fullName, u.email, u.avatarUrl,
            SUM(oi.unitPriceCents * oi.quantity),
            COUNT(DISTINCT c.id),
            COUNT(DISTINCT e.id)
        FROM Order o 
        JOIN o.items oi 
        JOIN Course c ON oi.entityId = c.id AND oi.entity = 'COURSE'
        JOIN c.createdBy u
        LEFT JOIN c.enrollments e
        WHERE o.status = 'PAID'
        GROUP BY u.id, u.fullName, u.email, u.avatarUrl
        ORDER BY SUM(oi.unitPriceCents * oi.quantity) DESC
        """)
    List<Object[]> findTopInstructorsByRevenue(Pageable pageable);

    // @Query("SELECT " +
    //    "FUNCTION('DATE_TRUNC', 'month', o.createdAt) as month, " +
    //    "SUM(CASE WHEN o.currency = 'VND' THEN o.totalCents ELSE 0 END) as revenueVND, " +
    //    "SUM(CASE WHEN o.currency = 'USD' THEN o.totalCents ELSE 0 END) as revenueUSD, " +
    //    "COUNT(o) as orderCount " +
    //    "FROM Order o " +
    //    "WHERE o.status = 'COMPLETED' AND o.createdAt >= :startDate " +
    //    "GROUP BY FUNCTION('DATE_TRUNC', 'month', o.createdAt) " +
    //    "ORDER BY month DESC")
    // List<Object[]> getRevenueByMonth(@Param("startDate") Instant startDate);

    @Query("""
        SELECT 
           FUNCTION('DATE_TRUNC', 'month', o.createdAt) as month, 
           SUM(CASE WHEN o.currency = 'VND' THEN o.totalCents ELSE 0 END) as revenueVND, 
           SUM(CASE WHEN o.currency = 'USD' THEN o.totalCents ELSE 0 END) as revenueUSD, 
           COUNT(o) as orderCount 
        FROM Order o 
        WHERE o.status = 'PAID' 
          AND o.createdAt >= :startDate 
        GROUP BY FUNCTION('DATE_TRUNC', 'month', o.createdAt) 
        ORDER BY month DESC
        """)
    List<Object[]> getRevenueByMonth(@Param("startDate") OffsetDateTime startDate);


}