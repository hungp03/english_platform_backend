package com.english.api.auth.repository;

import com.english.api.auth.model.VerificationToken;
import com.english.api.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 9/24/2025
 */
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiryDate <= :now")
    void deleteAllExpiredSince(@Param("now") Instant now);

    Optional<VerificationToken> findByUser(User user);
}


