package com.english.api.order.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.course.dto.response.CourseCheckoutResponse;
import com.english.api.course.service.CourseService;
import com.english.api.order.dto.request.CreateOrderRequest;
import com.english.api.order.dto.request.CreatePaymentRequest;
import com.english.api.order.dto.response.OrderResponse;
import com.english.api.order.mapper.OrderMapper;
import com.english.api.order.model.Order;
import com.english.api.order.model.OrderItem;
import com.english.api.order.model.enums.OrderStatus;
import com.english.api.order.model.enums.OrderItemEntityType;
import com.english.api.order.repository.OrderRepository;
import com.english.api.order.service.OrderService;
import com.english.api.user.model.User;
import com.english.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final CourseService courseService;
    private final OrderMapper orderMapper;

    // Valid status transitions mapping
    private static final Set<OrderStatus> PENDING_TRANSITIONS = Set.of(OrderStatus.PAID, OrderStatus.CANCELLED, OrderStatus.FAILED);
    private static final Set<OrderStatus> PAID_TRANSITIONS = Set.of(OrderStatus.REFUNDED);
    private static final Set<OrderStatus> FAILED_TRANSITIONS = Set.of(OrderStatus.PENDING);
    private static final Set<OrderStatus> CANCELLED_TRANSITIONS = Set.of(); // No transitions from cancelled
    private static final Set<OrderStatus> REFUNDED_TRANSITIONS = Set.of(); // No transitions from refunded

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating new order with {} items", request.items().size());
        
        // Get current authenticated user
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User currentUser = userService.findById(currentUserId);
        
        // Validate order items and calculate total
        Long totalCents = validateAndCalculateTotal(request);
        
        // Create order entity
        Order order = Order.builder()
                .user(currentUser)
                .status(OrderStatus.PENDING)
                .totalCents(totalCents)
                .build();
        
        // Create order items with optimized entity validation and title fetching
        List<OrderItem> orderItems = new ArrayList<>();
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.items()) {
            String entityTitle = getEntityTitleOptimized(itemRequest.entityType(), itemRequest.entityId());
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .entity(itemRequest.entityType())
                    .entityId(itemRequest.entityId())
                    .title(entityTitle)
                    .quantity(itemRequest.quantity())
                    .unitPriceCents(itemRequest.unitPriceCents())
                    .build();
            orderItems.add(orderItem);
        }
        
        order.setItems(orderItems);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        log.info("Successfully created order with ID: {}", savedOrder.getId());
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse processPayment(UUID orderId, CreatePaymentRequest paymentRequest) {
        log.info("Processing payment for order ID: {}", orderId);
        
        Order order = findOrderById(orderId);
        
        // Validate order can accept payment
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResourceInvalidException("Order must be in PENDING status to process payment. Current status: " + order.getStatus());
        }
        
        // Validate user authorization
        validateOrderAccess(order);
        
        // Note: Actual payment processing will be handled by PaymentService
        // This method prepares the order for payment processing
        
        log.info("Order {} is ready for payment processing", orderId);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        log.info("Updating order {} status to {}", orderId, newStatus);
        
        Order order = findOrderById(orderId);
        OrderStatus currentStatus = order.getStatus();
        
        // Validate status transition
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new ResourceInvalidException(
                String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
        
        // Update status
        order.setStatus(newStatus);
        
        // Set paid timestamp if transitioning to PAID
        if (newStatus == OrderStatus.PAID && order.getPaidAt() == null) {
            order.setPaidAt(OffsetDateTime.now());
        }
        
        Order savedOrder = orderRepository.save(order);
        
        log.info("Successfully updated order {} status from {} to {}", orderId, currentStatus, newStatus);
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    public PaginationResponse getOrdersByUser(UUID userId, Pageable pageable) {
        log.debug("Retrieving orders for user: {}", userId);
        
        // Validate user can access these orders
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            // TODO: Add admin role check when role system is available
            throw new AccessDeniedException("You can only access your own orders");
        }
        
        Page<Order> orderPage = orderRepository.findByUserIdWithItemsAndPayments(userId, pageable);
        
        return PaginationResponse.from(orderPage, pageable);
    }

    @Override
    public PaginationResponse getOrders(Pageable pageable, OrderStatus status, 
                                      OffsetDateTime startDate, OffsetDateTime endDate) {
        log.debug("Retrieving orders with filters - status: {}, startDate: {}, endDate: {}", 
                 status, startDate, endDate);
        
        // TODO: Add admin role check when role system is available
        // For now, allow access but this should be restricted to admins
        
        Page<Order> orderPage;
        
        // Apply filters based on provided parameters
        if (status != null && startDate != null && endDate != null) {
            orderPage = orderRepository.findByStatusAndCreatedAtBetween(status, startDate, endDate, pageable);
        } else if (status != null) {
            orderPage = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else if (startDate != null && endDate != null) {
            orderPage = orderRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        } else {
            orderPage = orderRepository.findAllWithItems(pageable);
        }
        
        return PaginationResponse.from(orderPage, pageable);
    }

    @Override
    public OrderResponse getOrderById(UUID orderId) {
        log.debug("Retrieving order by ID: {}", orderId);
        
        Order order = findOrderById(orderId);
        validateOrderAccess(order);
        
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {
        log.info("Cancelling order: {}", orderId);
        
        Order order = findOrderById(orderId);
        validateOrderAccess(order);
        
        // Validate order can be cancelled
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResourceInvalidException("Only PENDING orders can be cancelled. Current status: " + order.getStatus());
        }
        
        // Update status to cancelled
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        
        log.info("Successfully cancelled order: {}", orderId);
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    public boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return false; // No transition needed
        }
        
        return switch (currentStatus) {
            case PENDING -> PENDING_TRANSITIONS.contains(newStatus);
            case PAID -> PAID_TRANSITIONS.contains(newStatus);
            case FAILED -> FAILED_TRANSITIONS.contains(newStatus);
            case CANCELLED -> CANCELLED_TRANSITIONS.contains(newStatus);
            case REFUNDED -> REFUNDED_TRANSITIONS.contains(newStatus);
        };
    }

    @Override
    public Long validateAndCalculateTotal(CreateOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new ResourceInvalidException("Order must contain at least one item");
        }
        
        long totalCents = 0;
        
        for (CreateOrderRequest.OrderItemRequest item : request.items()) {
            // Validate item fields only - entity existence will be validated during order creation
            if (item.entityType() == null) {
                throw new ResourceInvalidException("Entity type is required for all items");
            }
            if (item.entityId() == null) {
                throw new ResourceInvalidException("Entity ID is required for all items");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new ResourceInvalidException("Quantity must be positive for all items");
            }
            if (item.unitPriceCents() == null || item.unitPriceCents() <= 0) {
                throw new ResourceInvalidException("Unit price must be positive for all items");
            }
            
            // Skip entity existence validation here to avoid duplicate queries
            // Entity validation will be performed in createOrder() when fetching titles
            
            // Calculate item total
            long itemTotal = item.quantity() * item.unitPriceCents();
            totalCents += itemTotal;
        }
        
        if (totalCents <= 0) {
            throw new ResourceInvalidException("Order total must be positive");
        }
        
        return totalCents;
    }



    /**
     * Validates and retrieves entity information in a single operation to optimize database queries
     */
    private CourseCheckoutResponse validateAndGetCourseInfo(UUID courseId) {
        return courseService.getCheckoutInfoById(courseId);
    }

    /**
     * Finds order by ID or throws ResourceNotFoundException
     */
    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
    }

    /**
     * Validates that current user can access the order
     */
    private void validateOrderAccess(Order order) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        
        // Users can access their own orders, admins can access all orders
        if (!order.getUser().getId().equals(currentUserId)) {
            // TODO: Add admin role check when role system is available
            throw new AccessDeniedException("You can only access your own orders");
        }
    }

    /**
     * Optimized method that validates entity existence and gets title in a single database query
     */
    private String getEntityTitleOptimized(OrderItemEntityType entityType, UUID entityId) {
        log.debug("Validating and fetching entity title: type={}, id={}", entityType, entityId);
        return switch (entityType) {
            case COURSE -> {
                CourseCheckoutResponse courseInfo = validateAndGetCourseInfo(entityId);
                yield courseInfo.title();
            }
            case SUBSCRIPTION -> {
                // TODO: Implement subscription validation when subscription module is available
                log.warn("SUBSCRIPTION validation not implemented yet for entity ID: {}", entityId);
                throw new ResourceInvalidException("SUBSCRIPTION entities are not supported yet");
            }
            case BUNDLE -> {
                // TODO: Implement bundle validation when bundle module is available
                log.warn("BUNDLE validation not implemented yet for entity ID: {}", entityId);
                throw new ResourceInvalidException("BUNDLE entities are not supported yet");
            }
        };
    }
}