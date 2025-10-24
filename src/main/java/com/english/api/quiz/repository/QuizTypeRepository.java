package com.english.api.quiz.repository;

import com.english.api.quiz.model.QuizType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuizTypeRepository extends JpaRepository<QuizType, UUID> {
    Optional<QuizType> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}