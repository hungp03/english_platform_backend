package com.english.api.order.dto.request;

import com.english.api.order.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Status is required")
        OrderStatus status,
        
        @Size(max = 1000, message = "Cancel reason must not exceed 1000 characters")
        String cancelReason
) {}
