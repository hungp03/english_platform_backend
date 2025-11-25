package com.english.api.quiz.repository;

import com.english.api.quiz.model.Quiz;
import com.english.api.quiz.model.enums.QuizSkill;
import com.english.api.quiz.model.enums.QuizStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID>, JpaSpecificationExecutor<Quiz> {
    Page<Quiz> findByQuizSectionIdAndStatus(UUID quizSectionId, QuizStatus status, Pageable pageable);

    @Query("""
            select distinct qz from Quiz qz
            left join fetch qz.quizType
            left join fetch qz.quizSection
            left join fetch qz.questions q
            left join fetch q.options
            where qz.id = :id
            order by q.orderIndex
            """)
    Optional<Quiz> findWithTreeById(UUID id);

    @Query("""
            select distinct qz from Quiz qz
            left join fetch qz.quizType
            left join fetch qz.quizSection
            left join fetch qz.questions q
            where qz.id = :id
            order by q.orderIndex
            """)
    Optional<Quiz> findWithTreeByIdWithoutOptions(UUID id);

    @Query("""
            select distinct qz from Quiz qz
            left join qz.quizType qt
            left join qz.quizSection qs
            where (:keyword is null or :keyword = '' 
                   or lower(qz.title) like lower(concat('%', :keyword, '%'))
                   or lower(qz.description) like lower(concat('%', :keyword, '%')))
            and (:quizTypeId is null or qt.id = :quizTypeId)
            and (:quizSectionId is null or qs.id = :quizSectionId)
            and (:status is null or qz.status = :status)
            and (:skill is null or qs.skill = :skill)
            """)
    Page<Quiz> searchQuizzes(@Param("keyword") String keyword,
                             @Param("quizTypeId") UUID quizTypeId,
                             @Param("quizSectionId") UUID quizSectionId,
                             @Param("status") QuizStatus status,
                             @Param("skill") QuizSkill skill,
                             Pageable pageable);

    @Query("""
            select distinct qz from Quiz qz
            left join qz.quizType qt
            left join qz.quizSection qs
            where qz.status = 'PUBLISHED'
            and (:quizTypeId is null or qt.id = :quizTypeId)
            and (:quizSectionId is null or qs.id = :quizSectionId)
            and (:skill is null or qs.skill = :skill)
            """)
    Page<Quiz> publicSearchQuizzes(@Param("quizTypeId") UUID quizTypeId,
                                   @Param("quizSectionId") UUID quizSectionId,
                                   @Param("skill") QuizSkill skill,
                                   Pageable pageable);
}
