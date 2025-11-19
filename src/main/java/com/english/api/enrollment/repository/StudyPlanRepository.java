package com.english.api.enrollment.repository;

import com.english.api.enrollment.model.StudyPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, UUID> {
    
    @Query("""
        SELECT sp FROM StudyPlan sp
        WHERE sp.user.id = :userId
        ORDER BY sp.createdAt DESC
        """)
    Page<StudyPlan> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = """
        SELECT sp FROM StudyPlan sp
        LEFT JOIN FETCH sp.schedules
        WHERE sp.user.id = :userId
        ORDER BY sp.createdAt DESC
        """,
        countQuery = """
        SELECT COUNT(sp) FROM StudyPlan sp
        WHERE sp.user.id = :userId
        """)
    Page<StudyPlan> findByUserIdWithSchedulesOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
        SELECT sp FROM StudyPlan sp
        LEFT JOIN FETCH sp.schedules
        WHERE sp.id = :id
        """)
    Optional<StudyPlan> findByIdWithSchedules(@Param("id") UUID id);
    
    @Query("""
        SELECT sp FROM StudyPlan sp
        LEFT JOIN FETCH sp.schedules
        WHERE sp.id = :id AND sp.user.id = :userId
        """)
    Optional<StudyPlan> findByIdAndUserIdWithSchedules(@Param("id") UUID id, @Param("userId") UUID userId);
    
    @Query("""
        SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END
        FROM StudyPlan sp
        WHERE sp.id = :id AND sp.user.id = :userId
        """)
    boolean existsByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

}
