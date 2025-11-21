package com.english.api.quiz.repository;

import com.english.api.quiz.model.QuizType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QuizTypeRepository extends JpaRepository<QuizType, UUID> {
    boolean existsByNameIgnoreCase(String name);
}