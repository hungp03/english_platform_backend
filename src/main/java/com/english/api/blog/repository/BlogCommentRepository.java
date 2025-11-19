package com.english.api.blog.repository;

import com.english.api.blog.model.BlogComment;
import com.english.api.blog.model.BlogPost;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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


    @Query("""
        SELECT c FROM BlogComment c 
        LEFT JOIN FETCH c.author 
        WHERE c.post.id = :postId 
        AND (:includeUnpublished = true OR c.published = true)
    """)
    Page<BlogComment> findCommentByPost(
        @Param("postId") UUID postId, 
        @Param("includeUnpublished") boolean includeUnpublished, 
        Pageable pageable
    );

    @Query("""
        SELECT c FROM BlogComment c 
        LEFT JOIN FETCH c.author 
        LEFT JOIN FETCH c.parent
        WHERE c.parent.id IN :parentIds
        AND (:includeUnpublished = true OR c.published = true)
        ORDER BY c.createdAt ASC
    """)
    List<BlogComment> findChildrenByParentIds(
        @Param("parentIds") List<UUID> parentIds,
        @Param("includeUnpublished") boolean includeUnpublished
    );

    @Query("SELECT COUNT(c) FROM BlogComment c WHERE c.post.id = :postId " +
       "AND (:includeUnpublished = true OR c.published = true)")
    long countAllByPostId(@Param("postId") UUID postId, 
                        @Param("includeUnpublished") boolean includeUnpublished);


    // Dùng cái này để lấy tất cả con (không cần phân trang)
    @Query("SELECT c FROM BlogComment c WHERE c.parent.id IN :parentIds")
    List<BlogComment> findByParentIdIn(@Param("parentIds") List<UUID> parentIds);


    Long countByPostIdAndParentIsNull(UUID postId);
    Long countByPostIdAndParentIsNullAndPublishedTrue(UUID postId);


    // Lấy tất cả con của các parent (có fetch author)
    @EntityGraph(attributePaths = {"author", "parent.author"})
    @Query("SELECT c FROM BlogComment c WHERE c.parent.id IN :parentIds")
    List<BlogComment> findRepliesByParentIds(@Param("parentIds") List<UUID> parentIds);


    @Query(
        value = "SELECT c FROM BlogComment c " +
                "WHERE c.post.id = :postId AND c.parent IS NULL AND c.published = true " +
                "ORDER BY c.createdAt DESC", 
        countQuery = "SELECT COUNT(c) FROM BlogComment c " +
                    "WHERE c.post.id = :postId AND c.parent IS NULL AND c.published = true" 
    )
    @EntityGraph(attributePaths = {"author", "post"})
    Page<BlogComment> findPublicRootComments(@Param("postId") UUID postId, Pageable pageable);

    @Query(
        value = "SELECT c FROM BlogComment c " +
                "WHERE c.post.id = :postId AND c.parent IS NULL " +
                "ORDER BY c.createdAt DESC", 
        countQuery = "SELECT COUNT(c) FROM BlogComment c " +
                    "WHERE c.post.id = :postId AND c.parent IS NULL"
    )
    @EntityGraph(attributePaths = {"author", "post"})
    Page<BlogComment> findAllRootComments(@Param("postId") UUID postId, Pageable pageable);
}
