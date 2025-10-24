package com.english.api.cart.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.cart.dto.response.CartItemResponse;
import com.english.api.cart.dto.response.CartResponse;
import com.english.api.cart.dto.response.CartCheckoutResponse;
import com.english.api.cart.dto.response.CourseInCartResponse;
import com.english.api.cart.mapper.CartItemMapper;
import com.english.api.cart.model.CartItem;
import com.english.api.cart.repository.CartItemRepository;
import com.english.api.cart.service.CartService;

import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceAlreadyOwnedException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.course.model.Course;
import com.english.api.course.model.enums.CourseStatus;
import com.english.api.course.repository.CourseRepository;
import com.english.api.order.repository.OrderRepository;
import com.english.api.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final int MAX_CART_SIZE = 10;

    private final CartItemRepository cartItemRepository;
    private final CourseRepository courseRepository;
    private final CartItemMapper cartItemMapper;
    private final OrderRepository orderRepository;

    @Transactional
    @Override
    public void addToCart(UUID courseId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        // Check if course exists and is published
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new ResourceInvalidException("Course is not available for purchase");
        }

        // Check if user has already purchased this course
        if (orderRepository.hasUserPurchasedCourse(currentUserId, courseId)) {
            throw new ResourceAlreadyOwnedException("You have already purchased this course");
        }

        // Check if already in cart
        if (cartItemRepository.existsByUserIdAndCourseId(currentUserId, courseId)) {
            throw new ResourceAlreadyExistsException("Course is already in your cart");
        }

        // Check cart size limit
        long currentCartSize = cartItemRepository.countByUserId(currentUserId);
        if (currentCartSize >= MAX_CART_SIZE) {
            throw new ResourceInvalidException(
                String.format("Cart is full. You can only have maximum %d courses in your cart at once.", MAX_CART_SIZE)
            );
        }

        // Add to cart
        CartItem cartItem = CartItem.builder()
                .user(User.builder().id(currentUserId).build())
                .course(course)
                .build();

        cartItemRepository.save(cartItem);
    }

    @Transactional
    @Override
    public void removeFromCart(UUID courseId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        // Check if item exists in cart
        if (!cartItemRepository.existsByUserIdAndCourseId(currentUserId, courseId)) {
            throw new ResourceNotFoundException("Course not found in your cart");
        }

        cartItemRepository.deleteByUserIdAndCourseId(currentUserId, courseId);
    }

    @Transactional
    @Override
    public void removeFromCart(List<UUID> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return;
        }

        UUID currentUserId = SecurityUtil.getCurrentUserId();
        
        // Batch delete all specified courses from user's cart
        cartItemRepository.deleteByUserIdAndCourseIdIn(currentUserId, courseIds);
    }

    @Override
    public CartResponse getMyCart() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        // Get all cart items (no pagination needed since max 10 items)
        List<CartItem> cartItems = cartItemRepository.findAllByUserIdWithPublishedCourses(currentUserId);

        // Batch check purchased courses for better performance
        List<UUID> courseIds = cartItems.stream()
                .map(cartItem -> cartItem.getCourse().getId())
                .toList();
        
        Set<UUID> purchasedCourseIds = orderRepository.findPurchasedCourseIds(currentUserId, courseIds);

        // Map to response with isPurchased check
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(cartItem -> {
                    Course course = cartItem.getCourse();
                    boolean isPurchased = purchasedCourseIds.contains(course.getId());
                    
                    CourseInCartResponse courseResponse = new CourseInCartResponse(
                        course.getId(),
                        course.getTitle(),
                        course.getSlug(),
                        course.getDescription(),
                        course.getThumbnail(),
                        course.getLanguage(),
                        course.getPriceCents(),
                        course.getCurrency(),
                        course.getCreatedBy() != null ? course.getCreatedBy().getFullName() : null,
                        isPurchased
                    );
                    
                    return new CartItemResponse(
                        cartItem.getId(),
                        courseResponse,
                        cartItem.getAddedAt()
                    );
                })
                .toList();

        // Calculate total count and price from the items we already have
        long totalPublishedCourses = itemResponses.size();
        Long totalPriceCents = cartItems.stream()
                .mapToLong(cartItem -> cartItem.getCourse().getPriceCents())
                .sum();

        // Get currency from first item (or default to USD)
        String currency = "USD";
        if (!cartItems.isEmpty()) {
            Course firstCourse = cartItems.getFirst().getCourse();
            currency = firstCourse.getCurrency() != null ? firstCourse.getCurrency() : "USD";
        }

        // Create CartSummary with cart limit information
        CartResponse.CartSummary summary = new CartResponse.CartSummary(
                totalPublishedCourses,
                totalPriceCents,
                currency,
                MAX_CART_SIZE,
                totalPublishedCourses >= MAX_CART_SIZE
        );

        return new CartResponse(itemResponses, summary);
    }

    @Transactional
    @Override
    public void clearCart() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        cartItemRepository.deleteAllByUserId(currentUserId);
    }

    @Override
    public List<CartCheckoutResponse> getCartCheckout() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        return cartItemRepository.findCoursesForCheckoutByUserId(currentUserId);
    }
}
