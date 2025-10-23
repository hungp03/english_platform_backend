package com.english.api.order.mapper;

import com.english.api.order.dto.response.OrderItemResponse;
import com.english.api.order.dto.response.OrderResponse;
import com.english.api.order.dto.response.PaymentResponse;
import com.english.api.order.dto.response.RefundResponse;
import com.english.api.order.model.Order;
import com.english.api.order.model.OrderItem;
import com.english.api.order.model.Payment;
import com.english.api.order.model.Refund;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Created by hungpham on 10/23/2025
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "invoices", ignore = true)
    OrderResponse toOrderResponse(Order order);

    List<OrderResponse> toOrderResponses(List<Order> orders);

    @Mapping(target = "entityType", source = "entity")
    @Mapping(target = "totalPriceCents", expression = "java(item.getQuantity() * item.getUnitPriceCents())")
    OrderItemResponse toOrderItemResponse(OrderItem item);

    List<OrderItemResponse> toOrderItemResponses(List<OrderItem> items);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "refunds", source = "refunds")
    PaymentResponse toPaymentResponse(Payment payment);

    List<PaymentResponse> toPaymentResponses(List<Payment> payments);

    @Mapping(target = "paymentId", source = "payment.id")
    RefundResponse toRefundResponse(Refund refund);

    List<RefundResponse> toRefundResponses(List<Refund> refunds);
}