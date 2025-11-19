package com.english.api.blog.repository;

import com.english.api.blog.model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {
    boolean existsByCategoriesId(UUID id);

    @Query("""
            SELECT DISTINCT p FROM BlogPost p
            LEFT JOIN p.categories c
            WHERE (:includeUnpublished = true OR p.published = true)
            AND (:keyword IS NULL OR LOWER(p.title) LIKE :keyword)
            AND (:authorId IS NULL OR p.author.id = :authorId)
            AND (:categoryId IS NULL OR c.id = :categoryId)
            AND (:categorySlug IS NULL OR c.slug = :categorySlug)
            AND (:fromDate IS NULL OR CAST(p.publishedAt AS date) >= :fromDate)
            AND (:toDate IS NULL OR CAST(p.publishedAt AS date) <= :toDate)
            """)
    Page<BlogPost> searchPosts(
            @Param("keyword") String keyword,
            @Param("authorId") UUID authorId,
            @Param("categoryId") UUID categoryId,
            @Param("categorySlug") String categorySlug,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("includeUnpublished") boolean includeUnpublished,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT p FROM BlogPost p
            LEFT JOIN FETCH p.author
            LEFT JOIN FETCH p.categories
            WHERE p.id IN :ids
            """)
    List<BlogPost> findByIdInWithAssociations(@Param("ids") List<UUID> ids);

    @EntityGraph(attributePaths = {"categories"})
    Optional<BlogPost> findBySlugAndPublishedIsTrue(String slug);
}