package com.english.api.quiz.repository;

import com.english.api.quiz.model.QuizType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuizTypeRepository extends JpaRepository<QuizType, UUID> {
    boolean existsByNameIgnoreCase(String name);
}