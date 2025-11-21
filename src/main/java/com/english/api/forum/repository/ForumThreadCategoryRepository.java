package com.english.api.forum.repository;

import com.english.api.forum.entity.ForumThread;
import com.english.api.forum.entity.ForumThreadCategory;
import com.english.api.forum.entity.ForumThreadCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ForumThreadCategoryRepository extends JpaRepository<ForumThreadCategory, ForumThreadCategoryId> {
    List<ForumThreadCategory> findByThread(ForumThread thread);

    @Modifying
    @Transactional
    void deleteByThread(ForumThread thread);
}
