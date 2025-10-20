package com.english.api.order.repository;

import com.english.api.order.model.Payment;
import com.english.api.order.model.PaymentProvider;
import com.english.api.order.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Payment entity with provider-specific queries
 * Provides data access operations for payments with filtering and audit capabilities
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Find payments by order ID
     * @param orderId the order ID to filter by
     * @return list of payments for the specified order
     */
    List<Payment> findByOrderIdOrderByCreatedAtDesc(UUID orderId);

    /**
     * Find payments by payment provider with pagination
     * @param provider the payment provider to filter by
     * @param pageable pagination parameters
     * @return page of payments for the specified provider
     */
    Page<Payment> findByProviderOrderByCreatedAtDesc(PaymentProvider provider, Pageable pageable);

    /**
     * Find payments by status with pagination
     * @param status the payment status to filter by
     * @param pageable pagination parameters
     * @return page of payments with the specified status
     */
    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);

    /**
     * Find payments by provider and status with pagination
     * @param provider the payment provider to filter by
     * @param status the payment status to filter by
     * @param pageable pagination parameters
     * @return page of payments matching both provider and status criteria
     */
    Page<Payment> findByProviderAndStatusOrderByCreatedAtDesc(
            PaymentProvider provider, 
            PaymentStatus status, 
            Pageable pageable);    
/**
     * Find payment by order ID and provider
     * @param orderId the order ID to filter by
     * @param provider the payment provider to filter by
     * @return optional payment matching the criteria
     */
    Optional<Payment> findByOrderIdAndProvider(UUID orderId, PaymentProvider provider);

    /**
     * Find payment by provider transaction ID
     * @param providerTxn the provider transaction ID
     * @return optional payment with the specified provider transaction ID
     */
    Optional<Payment> findByProviderTxn(String providerTxn);

    /**
     * Find payment by provider and provider transaction ID
     * @param provider the payment provider
     * @param providerTxn the provider transaction ID
     * @return optional payment matching both criteria
     */
    Optional<Payment> findByProviderAndProviderTxn(PaymentProvider provider, String providerTxn);

    /**
     * Find payments created within a date range with pagination
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return page of payments created within the specified date range
     */
    @Query("""
        SELECT p FROM Payment p 
        WHERE p.createdAt >= :startDate AND p.createdAt <= :endDate 
        ORDER BY p.createdAt DESC
        """)
    Page<Payment> findByCreatedAtBetween(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);

    /**
     * Find payments by provider within a date range with pagination
     * @param provider the payment provider to filter by
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return page of payments matching provider and date criteria
     */
    @Query("""
        SELECT p FROM Payment p 
        WHERE p.provider = :provider 
        AND p.createdAt >= :startDate AND p.createdAt <= :endDate 
        ORDER BY p.createdAt DESC
        """)
    Page<Payment> findByProviderAndCreatedAtBetween(
            @Param("provider") PaymentProvider provider,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);

    /**
     * Find payments by status within a date range with pagination
     * @param status the payment status to filter by
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return page of payments matching status and date criteria
     */
    @Query("""
        SELECT p FROM Payment p 
        WHERE p.status = :status 
        AND p.createdAt >= :startDate AND p.createdAt <= :endDate 
        ORDER BY p.createdAt DESC
        """)
    Page<Payment> findByStatusAndCreatedAtBetween(
            @Param("status") PaymentStatus status,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);   
 /**
     * Find transaction history for audit purposes - all payments with detailed information
     * @param pageable pagination parameters
     * @return page of payments with order and refund information for audit
     */
    @Query("""
        SELECT DISTINCT p FROM Payment p 
        LEFT JOIN FETCH p.order o 
        LEFT JOIN FETCH p.refunds 
        ORDER BY p.createdAt DESC
        """)
    Page<Payment> findTransactionHistoryWithDetails(Pageable pageable);

    /**
     * Find transaction history by provider for audit purposes
     * @param provider the payment provider to filter by
     * @param pageable pagination parameters
     * @return page of payments with order and refund information for the specified provider
     */
    @Query("""
        SELECT DISTINCT p FROM Payment p 
        LEFT JOIN FETCH p.order o 
        LEFT JOIN FETCH p.refunds 
        WHERE p.provider = :provider 
        ORDER BY p.createdAt DESC
        """)
    Page<Payment> findTransactionHistoryByProvider(
            @Param("provider") PaymentProvider provider, 
            Pageable pageable);

    /**
     * Find transaction history within date range for audit purposes
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return page of payments with order and refund information within the date range
     */
    @Query("""
        SELECT DISTINCT p FROM Payment p 
        LEFT JOIN FETCH p.order o 
        LEFT JOIN FETCH p.refunds 
        WHERE p.createdAt >= :startDate AND p.createdAt <= :endDate 
        ORDER BY p.createdAt DESC
        """)
    Page<Payment> findTransactionHistoryInDateRange(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);

    /**
     * Find successful payments by provider for revenue calculations
     * @param provider the payment provider to filter by
     * @param pageable pagination parameters
     * @return page of successful payments for the specified provider
     */
    @Query("""
        SELECT p FROM Payment p 
        WHERE p.provider = :provider AND p.status = 'SUCCESS' 
        ORDER BY p.confirmedAt DESC
        """)
    Page<Payment> findSuccessfulPaymentsByProvider(
            @Param("provider") PaymentProvider provider, 
            Pageable pageable);

    /**
     * Find successful payments within date range for revenue calculations
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination parameters
     * @return page of successful payments within the date range
     */
    @Query("""
        SELECT p FROM Payment p 
        WHERE p.status = 'SUCCESS' 
        AND p.confirmedAt >= :startDate AND p.confirmedAt <= :endDate 
        ORDER BY p.confirmedAt DESC
        """)
    Page<Payment> findSuccessfulPaymentsInDateRange(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);    /**

     * Count payments by provider and status for statistics
     * @param provider the payment provider
     * @param status the payment status
     * @return count of payments matching the criteria
     */
    Long countByProviderAndStatus(PaymentProvider provider, PaymentStatus status);

    /**
     * Count successful payments by provider for statistics
     * @param provider the payment provider
     * @return count of successful payments for the provider
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.provider = :provider AND p.status = 'SUCCESS'")
    Long countSuccessfulPaymentsByProvider(@Param("provider") PaymentProvider provider);

    /**
     * Calculate total amount of successful payments by provider
     * @param provider the payment provider
     * @return total amount in cents of successful payments for the provider
     */
    @Query("""
        SELECT COALESCE(SUM(p.amountCents), 0) FROM Payment p 
        WHERE p.provider = :provider AND p.status = 'SUCCESS'
        """)
    Long sumSuccessfulPaymentAmountByProvider(@Param("provider") PaymentProvider provider);

    /**
     * Calculate total amount of successful payments within date range
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return total amount in cents of successful payments within the date range
     */
    @Query("""
        SELECT COALESCE(SUM(p.amountCents), 0) FROM Payment p 
        WHERE p.status = 'SUCCESS' 
        AND p.confirmedAt >= :startDate AND p.confirmedAt <= :endDate
        """)
    Long sumSuccessfulPaymentAmountInDateRange(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);

    /**
     * Find payments with failed status for retry or investigation
     * @param pageable pagination parameters
     * @return page of failed payments
     */
    @Query("""
        SELECT p FROM Payment p 
        WHERE p.status = 'FAILED' 
        ORDER BY p.createdAt DESC
        """)
    Page<Payment> findFailedPayments(Pageable pageable);

    /**
     * Find payments by order with refunds for complete payment information
     * @param orderId the order ID
     * @return list of payments with refunds for the specified order
     */
    @Query("""
        SELECT DISTINCT p FROM Payment p 
        LEFT JOIN FETCH p.refunds 
        WHERE p.order.id = :orderId 
        ORDER BY p.createdAt DESC
        """)
    List<Payment> findByOrderIdWithRefunds(@Param("orderId") UUID orderId);

    /**
     * Find pending payments older than specified time for timeout handling
     * @param cutoffTime the cutoff time for pending payments
     * @param pageable pagination parameters
     * @return page of pending payments older than cutoff time
     */
    @Query("""
        SELECT p FROM Payment p 
        WHERE p.status IN ('INITIATED', 'PROCESSING') 
        AND p.createdAt < :cutoffTime 
        ORDER BY p.createdAt ASC
        """)
    Page<Payment> findPendingPaymentsOlderThan(
            @Param("cutoffTime") OffsetDateTime cutoffTime, 
            Pageable pageable);
}