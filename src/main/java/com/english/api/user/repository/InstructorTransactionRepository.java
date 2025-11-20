package com.english.api.user.repository;

import com.english.api.user.model.InstructorTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InstructorTransactionRepository extends JpaRepository<InstructorTransaction, UUID> {
    Page<InstructorTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
