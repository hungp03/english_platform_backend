package com.english.api.user.repository;

import com.english.api.user.model.InstructorRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorRequestRepository extends JpaRepository<InstructorRequest, UUID>, JpaSpecificationExecutor<InstructorRequest> {

    @Query("""
        SELECT DISTINCT ir FROM InstructorRequest ir
        LEFT JOIN FETCH ir.user u
        LEFT JOIN FETCH ir.reviewedBy rb
        LEFT JOIN FETCH ir.certificateProofs
        WHERE ir.user.id = :userId
        ORDER BY ir.requestedAt DESC
        """)
    List<InstructorRequest> findByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT ir FROM InstructorRequest ir
        LEFT JOIN FETCH ir.user u
        LEFT JOIN FETCH ir.reviewedBy rb
        LEFT JOIN FETCH ir.certificateProofs
        WHERE ir.user.id = :userId
        ORDER BY ir.requestedAt DESC
        LIMIT 1
        """)
    Optional<InstructorRequest> findLatestByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(ir) FROM InstructorRequest ir WHERE ir.user.id = :userId AND ir.status = 'PENDING'")
    int countPendingRequestsByUserId(@Param("userId") UUID userId);

    boolean existsByUserIdAndStatus(UUID userId, InstructorRequest.Status status);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT ir FROM InstructorRequest ir ORDER BY ir.requestedAt DESC")
    Page<InstructorRequest> findAllRequests(Pageable pageable);

    @Query("SELECT ir FROM InstructorRequest ir WHERE ir.status = :status ORDER BY ir.requestedAt DESC")
    Page<InstructorRequest> findByStatusOrderByRequestedAtDesc(@Param("status") InstructorRequest.Status status, Pageable pageable);

    @Query("""
        SELECT ir FROM InstructorRequest ir
        LEFT JOIN FETCH ir.user u
        LEFT JOIN FETCH ir.reviewedBy rb
        LEFT JOIN FETCH ir.certificateProofs
        WHERE ir.id = :requestId AND ir.user.id = :userId
        """)
    Optional<InstructorRequest> findByIdAndUserId(@Param("requestId") UUID requestId, @Param("userId") UUID userId);

    @Query("""
        SELECT ir FROM InstructorRequest ir
        LEFT JOIN FETCH ir.user u
        LEFT JOIN FETCH ir.reviewedBy rb
        LEFT JOIN FETCH ir.certificateProofs
        WHERE ir.id = :requestId
        """)
    Optional<InstructorRequest> findByIdWithDetails(@Param("requestId") UUID requestId);

    Long countByStatus(InstructorRequest.Status status);
}
