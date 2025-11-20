package com.english.api.user.repository;

import com.english.api.user.model.InstructorBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorBalanceRepository extends JpaRepository<InstructorBalance, UUID> {
    Optional<InstructorBalance> findByUserId(UUID userId);
}
