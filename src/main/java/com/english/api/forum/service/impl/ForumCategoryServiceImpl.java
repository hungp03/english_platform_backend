package com.english.api.forum.service.impl;

import com.english.api.forum.dto.request.ForumCategoryCreateRequest;
import com.english.api.forum.dto.request.ForumCategoryUpdateRequest;
import com.english.api.forum.dto.response.ForumCategoryResponse;
import com.english.api.forum.entity.ForumCategory;
import com.english.api.forum.repo.ForumCategoryRepository;
import com.english.api.forum.service.ForumCategoryService;
import com.english.api.forum.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForumCategoryServiceImpl implements ForumCategoryService {

  private final ForumCategoryRepository categoryRepo;

  @Override
  public List<ForumCategoryResponse> list() {
    return categoryRepo.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional
  public ForumCategoryResponse create(ForumCategoryCreateRequest request) {
    var category = ForumCategory.builder()
        .name(request.name())
        .slug(SlugUtil.slugify(request.slug()))
        .description(request.description())
        .build();
    return toDto(categoryRepo.save(category));
  }

  @Override
  @Transactional
  public ForumCategoryResponse update(UUID id, ForumCategoryUpdateRequest request) {
    var category = categoryRepo.findById(id).orElseThrow();

    if (request.name() != null) {
      category.setName(request.name());
      if (request.slug() == null || request.slug().isBlank()) {
        category.setSlug(SlugUtil.slugify(request.name()));
      }
    }

    if (request.slug() != null && !request.slug().isBlank()) {
      category.setSlug(SlugUtil.slugify(request.slug()));
    }

    if (request.description() != null) {
      category.setDescription(request.description());
    }

    return toDto(categoryRepo.save(category));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    categoryRepo.deleteById(id);
  }

  private ForumCategoryResponse toDto(ForumCategory category) {
    return new ForumCategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getDescription(), category.getCreatedAt());
  }
}
