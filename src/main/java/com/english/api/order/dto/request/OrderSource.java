package com.english.api.order.dto.request;

/**
 * Enum representing the source of an order
 * Created for tracking whether order comes from direct purchase or cart
 */
public enum OrderSource {
    DIRECT,     // Direct purchase from course page
    CART        // Purchase from shopping cart
}