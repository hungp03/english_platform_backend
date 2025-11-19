package com.english.api.order.mapper;

import com.english.api.order.dto.response.InvoiceResponse;
import com.english.api.order.dto.response.OrderDetailResponse;
import com.english.api.order.dto.response.OrderItemResponse;
import com.english.api.order.dto.response.OrderResponse;
import com.english.api.order.dto.response.OrderSummaryResponse;
import com.english.api.order.dto.response.PaymentResponse;
import com.english.api.order.dto.response.PaymentSummaryResponse;
import com.english.api.order.dto.response.UserBasicInfo;
import com.english.api.order.model.Invoice;
import com.english.api.order.model.Order;
import com.english.api.order.model.OrderItem;
import com.english.api.order.model.Payment;
import com.english.api.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Created by hungpham on 10/23/2025
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "items", source = "items")
//    @Mapping(target = "payments", ignore = true)
//    @Mapping(target = "invoices", ignore = true)
    OrderResponse toOrderResponse(Order order);

    List<OrderResponse> toOrderResponses(List<Order> orders);

    @Mapping(target = "itemCount", expression = "java(order.getItems() != null ? order.getItems().size() : 0)")
    OrderSummaryResponse toOrderSummaryResponse(Order order);

    List<OrderSummaryResponse> toOrderSummaryResponses(List<Order> orders);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "payments", source = "payments")
    OrderDetailResponse toOrderDetailResponse(Order order);

    UserBasicInfo toUserBasicInfo(User user);

    @Mapping(target = "entityType", source = "entity")
    @Mapping(target = "totalPriceCents", expression = "java(item.getQuantity() * item.getUnitPriceCents())")
    OrderItemResponse toOrderItemResponse(OrderItem item);

    List<OrderItemResponse> toOrderItemResponses(List<OrderItem> items);

    @Mapping(target = "orderId", source = "order.id")
    PaymentResponse toPaymentResponse(Payment payment);

    List<PaymentResponse> toPaymentResponses(List<Payment> payments);

    PaymentSummaryResponse toPaymentSummaryResponse(Payment payment);

    List<PaymentSummaryResponse> toPaymentSummaryResponses(List<Payment> payments);

    @Mapping(target = "orderId", source = "order.id")
    InvoiceResponse toInvoiceResponse(Invoice invoice);

    List<InvoiceResponse> toInvoiceResponses(List<Invoice> invoices);
}