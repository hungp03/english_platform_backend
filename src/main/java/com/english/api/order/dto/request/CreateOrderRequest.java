package com.english.api.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemRequest> items,
        
        @NotNull(message = "Order source is required")
        OrderSource orderSource,
        
        String voucherCode
) {

    public record OrderItemRequest(
            @NotNull(message = "Course ID is required")
            UUID courseId
    ) {}
}
