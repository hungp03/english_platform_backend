package com.english.api.cart.service;

import com.english.api.cart.dto.response.CartResponse;
import com.english.api.cart.dto.response.CartCheckoutResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
     * Remove multiple courses from user's cart in batch
     */
    @Transactional
    void removeFromCart(List<UUID> courseIds);

    /**
     * Get user's cart with all published courses (no pagination needed since max 10 items)
     */
    CartResponse getMyCart();

    /**
     * Clear all items from user's cart
     */
    @Transactional
    void clearCart();

    /**
     * Get all unpurchased courses in user's cart for checkout
     */
    List<CartCheckoutResponse> getCartCheckout();
}
