package com.english.api.user.repository;

import com.english.api.user.dto.response.InstructorBasicInfoResponse;
import com.english.api.user.model.InstructorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    @Query("SELECT new com.english.api.user.dto.response.InstructorBasicInfoResponse(" +
           "i.id, i.user.id, i.user.fullName, i.user.email, i.user.avatarUrl, i.experienceYears, i.isActive, i.createdAt) " +
           "FROM InstructorProfile i")
    Page<InstructorBasicInfoResponse> findAllBasicInfo(Pageable pageable);
    
    @Query("SELECT new com.english.api.user.dto.response.InstructorBasicInfoResponse(" +
           "i.id, i.user.id, i.user.fullName, i.user.email, i.user.avatarUrl, i.experienceYears, i.isActive, i.createdAt) " +
           "FROM InstructorProfile i " +
           "WHERE LOWER(i.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.user.fullName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<InstructorBasicInfoResponse> findAllBasicInfoWithSearch(@Param("search") String search, Pageable pageable);
}
