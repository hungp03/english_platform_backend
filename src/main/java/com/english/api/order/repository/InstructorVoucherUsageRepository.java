package com.english.api.order.repository;

import com.english.api.order.model.InstructorVoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InstructorVoucherUsageRepository extends JpaRepository<InstructorVoucherUsage, UUID> {

    @Query("""
        SELECT COUNT(u) FROM InstructorVoucherUsage u
        WHERE u.voucher.id = :voucherId AND u.user.id = :userId
        """)
    int countByVoucherIdAndUserId(@Param("voucherId") UUID voucherId, @Param("userId") UUID userId);

    boolean existsByVoucherIdAndUserId(UUID voucherId, UUID userId);

    @Query("""
        SELECT COALESCE(SUM(u.discountAmount), 0) FROM InstructorVoucherUsage u
        WHERE u.voucher.id = :voucherId
        """)
    java.math.BigDecimal getTotalDiscountByVoucherId(@Param("voucherId") UUID voucherId);
}
