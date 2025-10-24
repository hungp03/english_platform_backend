package com.english.api.quiz.repository;

import com.english.api.quiz.model.Quiz;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuizRepository extends JpaRepository<Quiz, UUID>, JpaSpecificationExecutor<Quiz> {
}