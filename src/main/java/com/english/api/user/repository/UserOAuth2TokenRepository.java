package com.english.api.user.repository;

import com.english.api.user.model.UserOAuth2Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserOAuth2TokenRepository extends JpaRepository<UserOAuth2Token, UUID> {
    Optional<UserOAuth2Token> findByUserId(UUID userId);
    Optional<UserOAuth2Token> findByUserIdAndProvider(UUID userId, String provider);
}
