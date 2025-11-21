package com.english.api.forum.repository;

import com.english.api.forum.entity.ForumPost;
import com.english.api.forum.entity.ForumThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, UUID> {
    @Query("""
            select distinct fp from ForumPost fp
            left join fetch fp.thread
            left join fetch fp.parent
            where fp.thread = :thread
            and fp.published = :published
            order by fp.createdAt asc
            """)
    Page<ForumPost> findByThreadAndPublishedOrderByCreatedAtAsc(@Param("thread") ForumThread thread,
                                                                @Param("published") boolean published,
                                                                Pageable pageable);

    @Query("""
            select distinct fp from ForumPost fp
            left join fetch fp.thread
            left join fetch fp.parent
            where fp.thread = :thread
            order by fp.createdAt asc
            """)
    Page<ForumPost> findByThreadOrderByCreatedAtAsc(@Param("thread") ForumThread thread,
                                                    Pageable pageable);

    @Query("""
            select distinct fp from ForumPost fp
            left join fetch fp.thread
            left join fetch fp.parent
            where fp.id in :ids
            """)
    List<ForumPost> findByIdIn(@Param("ids") Collection<UUID> ids);
  
    @Modifying
    @Query("delete from ForumPost fp where fp.parent = :parent")
    void deleteByParent(@Param("parent") ForumPost parent);

    @Query("""
        SELECT p FROM ForumPost p
        WHERE p.thread = :thread
        AND p.parent IS NULL
        AND (:includeUnpublished = true OR p.published = true)
        ORDER BY p.createdAt ASC
    """)
    Page<ForumPost> findRootsByThread(
        @Param("thread") ForumThread thread,
        @Param("includeUnpublished") boolean includeUnpublished,
        Pageable pageable
    );

    // 2. Tìm tất cả Post Con của danh sách Parent ID
    @Query("""
        SELECT p FROM ForumPost p
        LEFT JOIN FETCH p.parent
        WHERE p.parent.id IN :parentIds
        AND (:includeUnpublished = true OR p.published = true)
        ORDER BY p.createdAt ASC
    """)
    List<ForumPost> findChildrenByParentIds(
        @Param("parentIds") List<UUID> parentIds,
        @Param("includeUnpublished") boolean includeUnpublished
    );

    @Modifying
    @Transactional
    @Query("UPDATE ForumPost p SET p.parent = NULL WHERE p.thread = :thread")
    void unlinkParentsByThread(@Param("thread") ForumThread thread);

    @Modifying
    @Transactional
    @Query("DELETE FROM ForumPost p WHERE p.thread = :thread")
    void deleteAllByThread(@Param("thread") ForumThread thread);

    long countByParent(ForumPost parent);

    Optional<ForumPost> findFirstByThreadOrderByCreatedAtDesc(ForumThread thread);

    @Query("SELECT p.id FROM ForumPost p WHERE p.thread = :thread")
    List<UUID> findIdsByThread(@Param("thread") ForumThread thread);

    @Query("SELECT p.id FROM ForumPost p WHERE p.parent = :parent")
    List<UUID> findIdsByParent(@Param("parent") ForumPost parent);
}
