package com.english.api.forum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.english.api.forum.model.ForumThread;
import com.english.api.forum.model.ForumThreadCategory;
import com.english.api.forum.model.ForumThreadCategoryId;

import java.util.List;

@Repository
public interface ForumThreadCategoryRepository extends JpaRepository<ForumThreadCategory, ForumThreadCategoryId> {
    List<ForumThreadCategory> findByThread(ForumThread thread);

    @Modifying
    @Transactional
    void deleteByThread(ForumThread thread);
}
