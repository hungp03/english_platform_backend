package com.english.api.order.dto.response;

import java.util.UUID;

/**
 * DTO for course information in cart needed for checkout.
 * Contains only essential fields for payment preparation.
 */
public record CartCheckoutResponse(
    UUID id,
    String title,
    String thumbnail,
    Long priceCents,
    String currency
) {
}
