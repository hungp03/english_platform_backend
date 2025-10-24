package com.english.api.cart.controller;

import com.english.api.cart.dto.request.AddToCartRequest;
import com.english.api.cart.dto.response.CartResponse;
import com.english.api.cart.dto.response.CartCheckoutResponse;
import com.english.api.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Get current user's cart (no pagination needed since max 10 items)
     */
    @GetMapping
    public ResponseEntity<CartResponse> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    /**
     * Get all unpurchased courses in cart for checkout
     */
    @GetMapping("/checkout")
    public ResponseEntity<List<CartCheckoutResponse>> getCartCheckout() {
        return ResponseEntity.ok(cartService.getCartCheckout());
    }

    /**
     * Add a course to cart
     */
    @PostMapping
    public ResponseEntity<Void> addToCart(@Valid @RequestBody AddToCartRequest request) {
        cartService.addToCart(request.courseId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Remove a course from cart
     */
    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable UUID courseId) {
        cartService.removeFromCart(courseId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Clear entire cart
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
