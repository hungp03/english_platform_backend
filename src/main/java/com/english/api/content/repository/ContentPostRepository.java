package com.english.api.content.repository;

import com.english.api.content.model.ContentPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ContentPostRepository extends JpaRepository<ContentPost, UUID>, JpaSpecificationExecutor<ContentPost> {

    // 👉 Khi load post chi tiết (publicDetailBySlug), load luôn categories trong 1 query
    @EntityGraph(attributePaths = {"categories"})
    Optional<ContentPost> findBySlugAndPublishedIsTrue(String slug);

    boolean existsBySlug(String slug);

    Optional<ContentPost> findBySlug(String slug);

    // 👉 Khi tìm kiếm theo spec (search, publicList), load categories luôn
    @Override
    @EntityGraph(attributePaths = {"categories"})
    Page<ContentPost> findAll(Specification<ContentPost> spec, Pageable pageable);
}

// // package com.english.api.content.repository;

// // import com.english.api.content.model.ContentPost;
// // import org.springframework.data.jpa.repository.JpaRepository;
// // import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// // import java.util.Optional;
// // import java.util.UUID;

// // public interface ContentPostRepository extends JpaRepository<ContentPost, UUID>, JpaSpecificationExecutor<ContentPost> {
// //     Optional<ContentPost> findBySlugAndPublishedIsTrue(String slug);
// //     boolean existsBySlug(String slug);
// //     Optional<ContentPost> findBySlug(String slug);
// // }
// import com.english.api.content.model.ContentPost;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.data.jpa.repository.EntityGraph;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// import java.util.Optional;
// import java.util.UUID;

// public interface ContentPostRepository extends
//         JpaRepository<ContentPost, UUID>,
//         JpaSpecificationExecutor<ContentPost> {

//     @EntityGraph(attributePaths = {"categories"})
//     Optional<ContentPost> findBySlugAndPublishedIsTrue(String slug);

//     @EntityGraph(attributePaths = {"categories"})
//     Optional<ContentPost> findById(UUID id);

//     @EntityGraph(attributePaths = {"categories"})
//     Page<ContentPost> findAll(Specification<ContentPost> spec, Pageable pageable);
// }
