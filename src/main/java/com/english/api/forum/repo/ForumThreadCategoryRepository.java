package com.english.api.forum.repo;
import com.english.api.forum.entity.ForumThread;
import com.english.api.forum.entity.ForumThreadCategory;
import com.english.api.forum.entity.ForumThreadCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;
public interface ForumThreadCategoryRepository extends JpaRepository<ForumThreadCategory, ForumThreadCategoryId> {
  List<ForumThreadCategory> findByThread(ForumThread thread);
  void deleteByThread(ForumThread thread);
  
  @Query("""
    SELECT tc FROM ForumThreadCategory tc
    LEFT JOIN FETCH tc.category
    WHERE tc.thread.id IN :threadIds
    """)
  List<ForumThreadCategory> findByThreadIdsWithCategory(@Param("threadIds") List<UUID> threadIds);
}
