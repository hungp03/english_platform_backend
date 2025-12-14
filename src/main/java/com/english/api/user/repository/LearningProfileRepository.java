package com.english.api.user.repository;

import com.english.api.user.model.LearningProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LearningProfileRepository extends JpaRepository<LearningProfile, UUID> {
    Optional<LearningProfile> findByUserId(UUID userId);
}
