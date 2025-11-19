package com.english.api.notification.repository;

import com.english.api.notification.model.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {
    List<UserFcmToken> findByUserId(UUID userId);
    
    boolean existsByUserIdAndToken(UUID userId, String token);
}
