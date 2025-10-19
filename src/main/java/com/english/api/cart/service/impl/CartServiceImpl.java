package com.english.api.cart.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.cart.dto.response.CartItemResponse;
import com.english.api.cart.dto.response.CartPaginationResponse;
import com.english.api.cart.dto.response.CourseInCartResponse;
import com.english.api.cart.model.CartItem;
import com.english.api.cart.repository.CartItemRepository;
import com.english.api.cart.service.CartService;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.course.model.Course;
import com.english.api.course.repository.CourseRepository;
import com.english.api.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final CourseRepository courseRepository;

    @Transactional
    @Override
    public void addToCart(UUID courseId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        // Check if course exists and is published
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.isPublished()) {
            throw new ResourceInvalidException("Course is not available for purchase");
        }

        // Check if already in cart
        if (cartItemRepository.existsByUserIdAndCourseId(currentUserId, courseId)) {
            throw new ResourceAlreadyExistsException("Course is already in your cart");
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

    @Override
    public CartPaginationResponse getMyCart(Pageable pageable) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        // Get paginated cart items
        Page<CartItem> cartItemsPage = cartItemRepository.findByUserIdWithPublishedCourses(currentUserId, pageable);

        // Map to response
        Page<CartItemResponse> itemResponsesPage = cartItemsPage.map(this::mapToCartItemResponse);

        // Get total count of published courses in cart
        long totalPublishedCourses = cartItemRepository.countPublishedByUserId(currentUserId);

        // Calculate total price of all published courses in cart
        Long totalPriceCents = cartItemRepository.sumTotalPriceByUserId(currentUserId);

        // Get currency from first item (or default to USD)
        String currency = "USD";
        if (cartItemsPage.hasContent()) {
            Course firstCourse = cartItemsPage.getContent().getFirst().getCourse();
            currency = firstCourse.getCurrency() != null ? firstCourse.getCurrency() : "USD";
        }

        // Create Meta from PaginationResponse
        PaginationResponse.Meta meta = new PaginationResponse.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                itemResponsesPage.getTotalPages(),
                itemResponsesPage.getTotalElements()
        );

        // Create CartSummary
        CartPaginationResponse.CartSummary summary = new CartPaginationResponse.CartSummary(
                totalPublishedCourses,
                totalPriceCents,
                currency
        );

        return new CartPaginationResponse(meta, itemResponsesPage.getContent(), summary);
    }

    @Transactional
    @Override
    public void clearCart() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        cartItemRepository.deleteAllByUserId(currentUserId);
    }

    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        Course course = cartItem.getCourse();

        CourseInCartResponse courseResponse = new CourseInCartResponse(
                course.getId(),
                course.getTitle(),
                course.getSlug(),
                course.getDescription(),
                course.getThumbnail(),
                course.getLanguage(),
                course.getPriceCents(),
                course.getCurrency(),
                course.getCreatedBy() != null ? course.getCreatedBy().getFullName() : null
        );

        return new CartItemResponse(
                cartItem.getId(),
                courseResponse,
                cartItem.getAddedAt()
        );
    }
}
