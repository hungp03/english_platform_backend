package com.english.api.forum.repository;

import com.english.api.forum.model.ForumThreadSave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ForumThreadSaveRepository extends JpaRepository<ForumThreadSave, UUID> {
    
    boolean existsByUserIdAndThreadId(UUID userId, UUID threadId);
    
    Optional<ForumThreadSave> findByUserIdAndThreadId(UUID userId, UUID threadId);
    
    Page<ForumThreadSave> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    void deleteByThreadId(UUID threadId);

    @Query("""
        SELECT DISTINCT s 
        FROM ForumThreadSave s
        JOIN s.thread t
        LEFT JOIN com.english.api.forum.model.ForumThreadCategory tc ON tc.thread = t
        WHERE s.user.id = :userId
        AND (:categoryId IS NULL OR tc.category.id = :categoryId)
        AND (:locked IS NULL OR t.locked = :locked)
        AND (
             :keyword IS NULL 
             OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) 
             OR LOWER(t.bodyMd) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
        )
        ORDER BY s.createdAt DESC
    """)
    Page<ForumThreadSave> searchSavedThreads(
        @Param("userId") UUID userId, 
        @Param("keyword") String keyword, 
        @Param("categoryId") UUID categoryId, 
        @Param("locked") Boolean locked,
        Pageable pageable
    );
}