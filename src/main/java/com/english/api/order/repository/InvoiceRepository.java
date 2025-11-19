package com.english.api.order.repository;

import com.english.api.order.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByOrderId(UUID orderId);
    Optional<Invoice> findByNumber(String number);
    
    @Query("""
        SELECT i FROM Invoice i 
        WHERE i.order.id = :orderId AND i.order.user.id = :userId
        """)
    Optional<Invoice> findByOrderIdAndUserId(@Param("orderId") UUID orderId, @Param("userId") UUID userId);
}
