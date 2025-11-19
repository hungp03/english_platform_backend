package com.english.api.forum.repo;

import com.english.api.forum.entity.ForumPost;
import com.english.api.forum.entity.ForumThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
}
