package com.english.api.quiz.repository;

import com.english.api.quiz.enums.QuizStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.english.api.quiz.model.Quiz;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface QuizRepository extends JpaRepository<Quiz, UUID>, JpaSpecificationExecutor<Quiz> {
    Page<Quiz> findByQuizSectionIdAndStatus(UUID quizSectionId, QuizStatus status, Pageable pageable);

    @Query("""
        select distinct qz from Quiz qz
        left join fetch qz.quizType qt
        left join fetch qz.quizSection qs
        left join fetch qz.questions qu
        left join fetch qu.options opt
        where qz.id = :id
    """)
    Optional<Quiz> findWithTreeById(UUID id);
}
