package com.english.api.blog.repository;

import com.english.api.blog.model.BlogComment;
import com.english.api.blog.model.BlogPost;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, UUID> {

    @Query("SELECT c FROM BlogComment c WHERE c.post = :post")
    Page<BlogComment> findByPost(@Param("post") BlogPost post, Pageable pageable);

    @Query("SELECT c FROM BlogComment c WHERE c.post = :post AND c.published = true")
    Page<BlogComment> findByPostAndPublishedIsTrue(@Param("post") BlogPost post, Pageable pageable);

    @Query("""
            SELECT c FROM BlogComment c
            LEFT JOIN FETCH c.author
            LEFT JOIN FETCH c.post
            LEFT JOIN FETCH c.parent
            WHERE c.id IN :ids
            """)
    List<BlogComment> findByIdInWithAssociations(@Param("ids") List<UUID> ids);

    @Modifying
    @Transactional
    @Query("delete from BlogComment c where c.post.id = :postId")
    void deleteByPostId(@Param("postId") UUID postId);

    @Modifying
    @Query("delete from BlogComment c where c.parent = :parent")
    void deleteByParent(@Param("parent") BlogComment parent);
}
