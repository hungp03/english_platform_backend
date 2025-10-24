package com.english.api.order.dto.request;

import com.english.api.order.model.enums.OrderItemEntityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemRequest> items,
        
        @NotNull(message = "Order source is required")
        OrderSource orderSource
) {

    public record OrderItemRequest(
            @NotNull(message = "Entity type is required")
            OrderItemEntityType entityType,

            @NotNull(message = "Entity ID is required")
            UUID entityId,

            @NotNull(message = "Quantity is required")
            @Positive(message = "Quantity must be positive")
            Integer quantity,

            @NotNull(message = "Unit price is required")
            @Positive(message = "Unit price must be positive")
            Long unitPriceCents
    ) {}
}
