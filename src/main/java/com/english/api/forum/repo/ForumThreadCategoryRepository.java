package com.english.api.forum.repo;

import com.english.api.forum.entity.ForumThread;
import com.english.api.forum.entity.ForumThreadCategory;
import com.english.api.forum.entity.ForumThreadCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumThreadCategoryRepository extends JpaRepository<ForumThreadCategory, ForumThreadCategoryId> {
    List<ForumThreadCategory> findByThread(ForumThread thread);
}
