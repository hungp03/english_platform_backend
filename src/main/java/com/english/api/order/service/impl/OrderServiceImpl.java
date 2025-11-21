package com.english.api.order.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.cart.service.CartService;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceAlreadyOwnedException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.course.dto.response.CourseCheckoutResponse;
import com.english.api.course.service.CourseService;
import com.english.api.notification.service.NotificationService;
import com.english.api.order.dto.request.CreateOrderRequest;
import com.english.api.order.dto.request.OrderSource;
import com.english.api.order.dto.response.OrderDetailResponse;
import com.english.api.order.dto.response.OrderResponse;
import com.english.api.order.dto.response.OrderSummaryResponse;
import com.english.api.order.mapper.OrderMapper;
import com.english.api.order.model.Order;
import com.english.api.order.model.OrderItem;
import com.english.api.order.model.enums.OrderItemEntityType;
import com.english.api.order.model.enums.OrderStatus;
import com.english.api.order.repository.OrderRepository;
import com.english.api.order.service.OrderService;
import com.english.api.notification.service.NotificationService;
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
    private final CartService cartService;
    private final NotificationService notificationService;

    // Valid status transitions mapping
    private static final Set<OrderStatus> PENDING_TRANSITIONS = Set.of(OrderStatus.PAID, OrderStatus.CANCELLED,
            OrderStatus.FAILED);
    private static final Set<OrderStatus> PAID_TRANSITIONS = Set.of(OrderStatus.REFUNDED);
    private static final Set<OrderStatus> FAILED_TRANSITIONS = Set.of(OrderStatus.PENDING);
    private static final Set<OrderStatus> CANCELLED_TRANSITIONS = Set.of(); // No transitions from cancelled
    private static final Set<OrderStatus> REFUNDED_TRANSITIONS = Set.of(); // No transitions from refunded

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Get current authenticated user
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User currentUser = userService.findById(currentUserId);
        // Check if user has already purchased any of the courses in the order
        validateNoPurchasedCourses(currentUserId, request);
        // Validate order items and calculate total
        Long totalCents = validateAndCalculateTotal(request);
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
        // If order is from cart, remove the purchased items from cart after successful
        if (request.orderSource() == OrderSource.CART) {
            removeOrderedItemsFromCart(request.items());
        }

        notificationService.sendNotification(
                currentUserId,
                "Đơn hàng đã được tạo thành công",
                "Đơn hàng #" + savedOrder.getId() + " đã được khởi tạo thành công.");

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus, String cancelReason) {
        Order order = findOrderById(orderId);
        OrderStatus currentStatus = order.getStatus();
        // Validate status transition
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new ResourceInvalidException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
        // Update status
        order.setStatus(newStatus);
        // Set paid timestamp if transitioning to PAID
        if (newStatus == OrderStatus.PAID && order.getPaidAt() == null) {
            order.setPaidAt(OffsetDateTime.now());
        }
        // Set cancel reason and timestamp if transitioning to CANCELLED
        if (newStatus == OrderStatus.CANCELLED) {
            order.setCancelReason(cancelReason);
            order.setCancelAt(OffsetDateTime.now());
            
            // Send notification about order cancellation
            notificationService.sendNotification(
                order.getUser().getId(),
                "Đã hủy đơn hàng",
                "Đơn hàng #" + order.getId() + " đã được hủy. " + 
                (cancelReason != null ? "Lý do: " + cancelReason : "")
            );
        }
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    public PaginationResponse getOrdersByUser(Pageable pageable) {
        // Get current authenticated user ID from security context
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        // Use method without eager loading for better performance in listing
        Page<Order> orderPage = orderRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, pageable);
        // Map entities to summary DTOs (without items details)
        Page<OrderSummaryResponse> orderSummaryPage = orderPage.map(orderMapper::toOrderSummaryResponse);
        return PaginationResponse.from(orderSummaryPage, pageable);
    }

    @Override
    public PaginationResponse getOrders(Pageable pageable, OrderStatus status,
            OffsetDateTime startDate, OffsetDateTime endDate) {
        Page<Order> orderPage;
        // Apply filters based on provided parameters
        if (status != null && startDate != null && endDate != null) {
            orderPage = orderRepository.findByStatusAndCreatedAtBetween(status, startDate, endDate, pageable);
        } else if (status != null) {
            orderPage = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else if (startDate != null && endDate != null) {
            orderPage = orderRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        } else {
            orderPage = orderRepository.findAllOrderByCreatedAtDesc(pageable);
        }
        // Map entities to summary DTOs (without items details) for better performance
        Page<OrderSummaryResponse> orderSummaryPage = orderPage.map(orderMapper::toOrderSummaryResponse);
        return PaginationResponse.from(orderSummaryPage, pageable);
    }

    @Override
    public OrderResponse getOrderById(UUID orderId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        // Use method that fetches items with user authorization built-in
        Order order = orderRepository.findByIdAndUserIdWithItems(orderId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + orderId + " or access denied"));

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public OrderDetailResponse getMyOrderDetailById(UUID orderId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        // Get order with user and items in one query (with user authorization built-in)
        Order orderWithItems = orderRepository.findByIdAndUserIdWithItems(orderId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + orderId + " or access denied"));
        // Get payments in separate query (with user authorization built-in)
        Order orderWithPayments = orderRepository.findByIdAndUserIdWithPayments(orderId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + orderId + " or access denied"));
        // Merge the data: use orderWithItems as base and add payments
        orderWithItems.setPayments(orderWithPayments.getPayments());
        return orderMapper.toOrderDetailResponse(orderWithItems);
    }

    @Override
    public OrderDetailResponse getOrderDetailByIdForAdmin(UUID orderId) {
        // Get order with items in one query (no user restriction for admin)
        Order orderWithItems = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        // Get payments in separate query (no user restriction for admin)
        Order orderWithPayments = orderRepository.findByIdWithPayments(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        // Merge the data: use orderWithItems as base and add payments
        orderWithItems.setPayments(orderWithPayments.getPayments());
        return orderMapper.toOrderDetailResponse(orderWithItems);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID orderId, String cancelReason) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        // Find order with user authorization built-in
        Order order = orderRepository.findByIdAndUserIdWithItems(orderId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + orderId + " or access denied"));
        // Validate order can be cancelled
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResourceInvalidException(
                    "Only PENDING orders can be cancelled. Current status: " + order.getStatus());
        }
        // Update status to cancelled
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(cancelReason);
        order.setCancelAt(OffsetDateTime.now());
        Order savedOrder = orderRepository.save(order);
        notificationService.sendNotification(
            currentUserId,
            "Đã hủy đơn hàng",
            "Đơn hàng #" + order.getId() + " đã được hủy. " + 
            (cancelReason != null ? "Lý do: " + cancelReason : "")
        );
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
            // Validate item fields only - entity existence will be validated during order
            // creation
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
     * Validates and retrieves entity information in a single operation to optimize
     * database queries
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
     * Optimized method that validates entity existence and gets title in a single
     * database query
     */
    private String getEntityTitleOptimized(OrderItemEntityType entityType, UUID entityId) {
        return switch (entityType) {
            case COURSE -> {
                CourseCheckoutResponse courseInfo = validateAndGetCourseInfo(entityId);
                yield courseInfo.title();
            }
            case SUBSCRIPTION -> {
                throw new ResourceInvalidException("SUBSCRIPTION entities are not supported yet");
            }
            case BUNDLE -> {
                throw new ResourceInvalidException("BUNDLE entities are not supported yet");
            }
        };
    }

    /**
     * Validates that user hasn't already purchased any courses in the order
     */
    private void validateNoPurchasedCourses(UUID userId, CreateOrderRequest request) {
        for (CreateOrderRequest.OrderItemRequest item : request.items()) {
            // Only check for COURSE entities
            if (item.entityType() == OrderItemEntityType.COURSE) {
                if (orderRepository.hasUserPurchasedCourse(userId, item.entityId())) {
                    throw new ResourceAlreadyOwnedException(
                            String.format(
                                    "You have already purchased the course with ID: %s. Please remove it from your order.",
                                    item.entityId()));
                }
            }
        }
    }

    /**
     * Removes ordered items from user's cart using batch delete
     * Only removes COURSE items as other entity types are not supported in cart yet
     */
    private void removeOrderedItemsFromCart(List<CreateOrderRequest.OrderItemRequest> items) {
        List<UUID> courseIdsToRemove = items.stream()
                .filter(item -> item.entityType() == OrderItemEntityType.COURSE)
                .map(CreateOrderRequest.OrderItemRequest::entityId)
                .toList();

        if (!courseIdsToRemove.isEmpty()) {
            try {
                cartService.removeFromCart(courseIdsToRemove);
            } catch (Exception e) {
                log.warn("Failed to batch remove courses from cart: {}", e.getMessage());
            }
        }
    }
}