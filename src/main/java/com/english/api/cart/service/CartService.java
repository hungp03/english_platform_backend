package com.english.api.cart.service;

import com.english.api.cart.dto.response.CartPaginationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface CartService {

    /**
     * Add a published course to user's cart
     */
    @Transactional
    void addToCart(UUID courseId);

    /**
     * Remove a course from user's cart
     */
    @Transactional
    void removeFromCart(UUID courseId);

    /**
     * Get user's cart with all published courses (paginated)
     */
    CartPaginationResponse getMyCart(Pageable pageable);

    /**
     * Clear all items from user's cart
     */
    @Transactional
    void clearCart();
}
