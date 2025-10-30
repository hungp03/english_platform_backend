package com.english.api.user.repository;

import com.english.api.user.model.InstructorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for InstructorProfile entity
 * Created by hungpham on 10/29/2025
 */
@Repository
public interface InstructorProfileRepository extends JpaRepository<InstructorProfile, UUID> {
    
    Optional<InstructorProfile> findByUserId(UUID userId);
    
    boolean existsByUserId(UUID userId);
    
    void deleteByUserId(UUID userId);
}
