package com.english.api.user.repository;

import com.english.api.user.model.WithdrawalRequest;
import com.english.api.user.model.enums.WithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, UUID> {
    Page<WithdrawalRequest> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Page<WithdrawalRequest> findByStatusOrderByCreatedAtAsc(WithdrawalStatus status, Pageable pageable);
}
