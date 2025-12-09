package com.english.api.order.repository;

import com.english.api.order.model.InstructorVoucher;
import com.english.api.order.model.enums.VoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorVoucherRepository extends JpaRepository<InstructorVoucher, UUID> {

    Optional<InstructorVoucher> findByCode(String code);

    @Query("""
        SELECT v FROM InstructorVoucher v
        LEFT JOIN FETCH v.applicableCourses
        WHERE v.code = :code
        """)
    Optional<InstructorVoucher> findByCodeWithCourses(@Param("code") String code);

    boolean existsByCode(String code);

    Page<InstructorVoucher> findByInstructorIdOrderByCreatedAtDesc(UUID instructorId, Pageable pageable);

    @Query("""
        SELECT v FROM InstructorVoucher v
        WHERE v.instructor.id = :instructorId
        AND (:status IS NULL OR v.status = :status)
        ORDER BY v.createdAt DESC
        """)
    Page<InstructorVoucher> findByInstructorIdAndStatus(
            @Param("instructorId") UUID instructorId,
            @Param("status") VoucherStatus status,
            Pageable pageable);

    @Query("""
        SELECT v FROM InstructorVoucher v
        LEFT JOIN FETCH v.applicableCourses
        WHERE v.id = :id AND v.instructor.id = :instructorId
        """)
    Optional<InstructorVoucher> findByIdAndInstructorIdWithCourses(
            @Param("id") UUID id,
            @Param("instructorId") UUID instructorId);

    @Query("""
        SELECT v FROM InstructorVoucher v
        LEFT JOIN FETCH v.applicableCourses ac
        WHERE v.code = :code
        AND v.status = 'ACTIVE'
        AND v.startDate <= :now
        AND v.endDate > :now
        AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)
        """)
    Optional<InstructorVoucher> findValidVoucherByCode(
            @Param("code") String code,
            @Param("now") OffsetDateTime now);

    @Query("""
        SELECT v FROM InstructorVoucher v
        WHERE v.status = 'ACTIVE'
        AND v.endDate < :now
        """)
    List<InstructorVoucher> findExpiredVouchers(@Param("now") OffsetDateTime now);
}
