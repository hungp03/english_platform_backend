package com.english.api.order.controller;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.order.dto.request.CreateOrderRequest;
import com.english.api.order.dto.request.CreatePaymentRequest;
import com.english.api.order.dto.response.OrderResponse;
import com.english.api.order.model.enums.OrderStatus;
import com.english.api.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * REST Controller for order management operations
 * Provides endpoints for order creation, retrieval, listing, and cancellation
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get order by ID with authorization check
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")

    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * List orders with pagination and filtering
     * Requirements: 5.1, 5.4 - Order listing with pagination
     * 
     * @param pageable pagination parameters
     * @param status optional status filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @return paginated response of filtered orders
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse> getOrders(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        
        PaginationResponse response = orderService.getOrders(pageable, status, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get orders for the current authenticated user
     * Requirements: 5.2 - User-specific order retrieval
     * 
     * @param pageable pagination parameters
     * @return paginated response of user orders
     */
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse> getMyOrders(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        PaginationResponse response = orderService.getOrdersByUser(currentUserId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get orders for a specific user (admin only)
     * Requirements: 5.2 - User-specific order retrieval with admin access
     * 
     * @param userId the user ID to get orders for
     * @param pageable pagination parameters
     * @return paginated response of user orders
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse> getUserOrders(
            @PathVariable UUID userId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        
        PaginationResponse response = orderService.getOrdersByUser(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an order
     * Requirements: 5.3 - Order cancellation with validation
     * 
     * @param id the order ID to cancel
     * @return the updated order response
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID id) {
        OrderResponse response = orderService.cancelOrder(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update order status (admin only)
     * Requirements: 5.3 - Order status management
     * 
     * @param id the order ID to update
     * @param status the new status
     * @return the updated order response
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status) {
        
        OrderResponse response = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Process payment for an order
     * Requirements: 5.1 - Payment processing
     * 
     * @param id the order ID to process payment for
     * @param paymentRequest the payment request
     * @return the payment response
     */
    @PostMapping("/{id}/payment")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> processPayment(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePaymentRequest paymentRequest) {
        
        OrderResponse response = orderService.processPayment(id, paymentRequest);
        return ResponseEntity.ok(response);
    }
}