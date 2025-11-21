package com.english.api.order.repository;

import com.english.api.order.model.Payment;
import com.english.api.order.model.enums.PaymentProvider;
import com.english.api.order.model.enums.PaymentStatus;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findTopByOrderIdAndProviderOrderByCreatedAtDesc(UUID orderId, PaymentProvider provider);

    List<Payment> findByOrderIdOrderByCreatedAtDesc(UUID orderId);

    @EntityGraph(attributePaths = {"order", "order.user", "order.items"})
    @Query("SELECT p FROM Payment p WHERE p.providerTxn = :providerTxn")
    Optional<Payment> findByProviderTxnWithOrderDetails(@Param("providerTxn") String providerTxn);


    @EntityGraph(attributePaths = {"order", "order.user", "order.items"})
    @Query("SELECT p FROM Payment p WHERE p.provider = :provider AND p.providerTxn = :providerTxn")
    Optional<Payment> findByProviderAndProviderTxnWithOrderDetails(@Param("provider") PaymentProvider provider, @Param("providerTxn") String providerTxn);
}