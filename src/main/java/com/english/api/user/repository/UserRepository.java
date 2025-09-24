package com.english.api.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.english.api.user.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u.isActive FROM User u WHERE u.id = :userId")
    Boolean isUserActive(@Param("userId") UUID userId);

    boolean existsByEmail(String email);
}
