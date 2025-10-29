package com.english.api.order.controller;


import com.english.api.common.dto.PaginationResponse;
import com.english.api.order.dto.request.CreateOrderRequest;
import com.english.api.order.dto.response.OrderDetailResponse;
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
     * Get order by ID with full details including user info, items, and payments
     */
    @GetMapping("/my-order/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderDetailResponse> getMyOrderById(@PathVariable UUID id) {
        OrderDetailResponse response = orderService.getMyOrderDetailById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * List orders with pagination and filtering
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
     * Get orders summary for the current authenticated user
     */
    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginationResponse> getMyOrders(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        PaginationResponse response = orderService.getOrdersByUser(pageable);
        return ResponseEntity.ok(response);
    }


    /**
     * Cancel an order
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID id) {
        OrderResponse response = orderService.cancelOrder(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update order status (admin only)
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status) {
        
        OrderResponse response = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(response);
    }
}