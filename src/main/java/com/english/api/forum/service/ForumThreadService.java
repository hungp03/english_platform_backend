package com.english.api.forum.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumThreadCreateRequest;
import com.english.api.forum.dto.response.ForumThreadResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ForumThreadService {
  PaginationResponse listPublic(String keyword, java.util.UUID categoryId, Boolean locked, Pageable pageable);
  ForumThreadResponse getBySlug(String slug);
  void increaseView(UUID threadId);
  ForumThreadResponse create(ForumThreadCreateRequest req);
  ForumThreadResponse adminLock(UUID id, boolean lock);
  ForumThreadResponse lockByOwner(UUID id, boolean lock);
  void delete(UUID id);
}
