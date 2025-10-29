package com.english.api.order.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.order.dto.request.CreateOrderRequest;
import com.english.api.order.dto.response.OrderDetailResponse;
import com.english.api.order.dto.response.OrderResponse;
import com.english.api.order.model.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface OrderService {

    /**
     * Creates a new order with validation and business logic
     */
    @Transactional
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Updates order status with proper state transition validation
     */
    @Transactional
    OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus);

    /**
     * Retrieves orders for the current authenticated user with pagination
     */
    PaginationResponse getOrdersByUser(Pageable pageable);



    /**
     * Retrieves orders with filtering and pagination
     */
    PaginationResponse getOrders(Pageable pageable, OrderStatus status, 
                                OffsetDateTime startDate, OffsetDateTime endDate);

    /**
     * Retrieves a single order by ID with authorization check
     */
    OrderResponse getOrderById(UUID orderId);

    /**
     * Retrieves detailed order information by ID with user info, items, and payments
     */
    OrderDetailResponse getMyOrderDetailById(UUID orderId);

    /**
     * Cancels an order with proper validation
     */
    @Transactional
    OrderResponse cancelOrder(UUID orderId);

    /**
     * Validates order state transition
     */
    boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus);

    /**
     * Validates order items and calculates total
     */
    Long validateAndCalculateTotal(CreateOrderRequest request);
}