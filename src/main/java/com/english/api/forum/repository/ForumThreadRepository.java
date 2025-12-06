package com.english.api.forum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.english.api.forum.model.ForumThread;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ForumThreadRepository extends JpaRepository<ForumThread, UUID> {

    Optional<ForumThread> findBySlug(String slug);

  @Query("""

    SELECT DISTINCT t FROM ForumThread t
      LEFT JOIN com.english.api.forum.model.ForumThreadCategory tc ON tc.thread = t
     WHERE (:categoryId IS NULL OR tc.category.id = :categoryId)
       AND (:locked IS NULL OR t.locked = :locked)
       AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(t.bodyMd) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR :keyword IS NULL)
  """)
  Page<ForumThread> search(@Param("keyword") String keyword,
                           @Param("categoryId") UUID categoryId,
                           @Param("locked") Boolean locked,
                           Pageable pageable);
  

  @Query("""
        SELECT DISTINCT t FROM ForumThread t
        LEFT JOIN com.english.api.forum.model.ForumThreadCategory tc ON tc.thread = t
        WHERE t.authorId = :authorId
        AND (:categoryId IS NULL OR tc.category.id = :categoryId)
        AND (:locked IS NULL OR t.locked = :locked)
        AND (
             :keyword IS NULL 
             OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) 
             OR LOWER(t.bodyMd) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
        )
    """)
    Page<ForumThread> searchByAuthor(
            @Param("authorId") UUID authorId,
            @Param("keyword") String keyword,
            @Param("categoryId") UUID categoryId,
            @Param("locked") Boolean locked,
            Pageable pageable
    );
  
  Page<ForumThread> findByAuthorIdOrderByCreatedAtDesc(UUID authorId, Pageable pageable);
}
