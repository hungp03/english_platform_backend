package com.english.api.forum.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumPostCreateRequest;
import com.english.api.forum.dto.response.ForumPostResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ForumPostService {
  PaginationResponse listByThread(UUID threadId, Pageable pageable, boolean onlyPublished);
  ForumPostResponse create(UUID threadId, ForumPostCreateRequest req);
  ForumPostResponse hide(UUID postId);
  ForumPostResponse show(UUID postId);
  void deleteByOwner(UUID postId);
  void adminDelete(UUID postId);
}
