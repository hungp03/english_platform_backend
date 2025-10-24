package com.english.api.forum.service;

import com.english.api.forum.dto.request.ForumCategoryCreateRequest;
import com.english.api.forum.dto.request.ForumCategoryUpdateRequest;
import com.english.api.forum.dto.response.ForumCategoryResponse;

import java.util.List;
import java.util.UUID;

public interface ForumCategoryService {
  List<ForumCategoryResponse> list();
  ForumCategoryResponse create(ForumCategoryCreateRequest req);
  ForumCategoryResponse update(UUID id, ForumCategoryUpdateRequest req);
  void delete(UUID id);
}
