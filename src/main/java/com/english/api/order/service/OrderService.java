package com.english.api.order.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.order.dto.request.CreateOrderRequest;
import com.english.api.order.dto.request.CreatePaymentRequest;
import com.english.api.order.dto.response.OrderResponse;
import com.english.api.order.model.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Service interface for order lifecycle management
 * Provides business logic for order creation, status updates, and validation
 * Requirements: 1.1, 1.2, 1.3, 7.1, 7.2
 */
public interface OrderService {

    /**
     * Creates a new order with validation and business logic
     * Requirements: 1.1 - Order creation with validation
     * @param request the order creation request containing items
     * @return the created order response
     */
    @Transactional
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Processes payment for an existing order
     * Requirements: 1.2 - Order state transitions
     * @param orderId the order ID to process payment for
     * @param paymentRequest the payment processing request
     * @return the updated order response
     */
    @Transactional
    OrderResponse processPayment(UUID orderId, CreatePaymentRequest paymentRequest);

    /**
     * Updates order status with proper state transition validation
     * Requirements: 1.2, 7.2 - Order validation and state transitions
     * @param orderId the order ID to update
     * @param newStatus the new status to set
     * @return the updated order response
     */
    @Transactional
    OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus);

    /**
     * Retrieves orders for a specific user with pagination
     * Requirements: 7.1 - Centralized business logic
     * @param userId the user ID to filter orders by
     * @param pageable pagination parameters
     * @return paginated response of user orders
     */
    PaginationResponse getOrdersByUser(UUID userId, Pageable pageable);

    /**
     * Retrieves orders with filtering and pagination
     * Requirements: 7.1 - Centralized business logic
     * @param pageable pagination parameters
     * @param status optional status filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @return paginated response of filtered orders
     */
    PaginationResponse getOrders(Pageable pageable, OrderStatus status, 
                                OffsetDateTime startDate, OffsetDateTime endDate);

    /**
     * Retrieves a single order by ID with authorization check
     * Requirements: 7.1 - Centralized business logic
     * @param orderId the order ID to retrieve
     * @return the order response
     */
    OrderResponse getOrderById(UUID orderId);

    /**
     * Cancels an order with proper validation
     * Requirements: 1.3, 7.2 - Order validation and state transitions
     * @param orderId the order ID to cancel
     * @return the updated order response
     */
    @Transactional
    OrderResponse cancelOrder(UUID orderId);

    /**
     * Validates order state transition
     * Requirements: 7.2 - Order validation and state transitions
     * @param currentStatus the current order status
     * @param newStatus the desired new status
     * @return true if transition is valid, false otherwise
     */
    boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus);

    /**
     * Validates order items and calculates total
     * Requirements: 1.1 - Order creation with validation
     * @param request the order creation request
     * @return the calculated total in cents
     */
    Long validateAndCalculateTotal(CreateOrderRequest request);
}