package com.english.api.course.repository;

import com.english.api.course.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {
    
    Optional<Skill> findByName(String name);
    
    boolean existsByNameIgnoreCase(String name);
    
    @Query("SELECT s FROM Skill s WHERE s.name IN :names")
    Set<Skill> findByNameIn(@Param("names") List<String> names);
    
    @Query("SELECT COUNT(c) > 0 FROM Course c JOIN c.skills s WHERE s.id = :skillId")
    boolean isSkillUsedByCourses(@Param("skillId") UUID skillId);
}
