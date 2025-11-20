package com.english.api.user.repository;

import com.english.api.user.model.InstructorBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorBankAccountRepository extends JpaRepository<InstructorBankAccount, UUID> {
    Optional<InstructorBankAccount> findByUserId(UUID userId);
}
