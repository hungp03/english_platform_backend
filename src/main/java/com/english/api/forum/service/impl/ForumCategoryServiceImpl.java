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

  private final ForumCategoryRepository repo;

  @Override
  public List<ForumCategoryResponse> list() {
    return repo.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional
  public ForumCategoryResponse create(ForumCategoryCreateRequest req) {
    var cat = ForumCategory.builder()
        .name(req.name())
        .slug(SlugUtil.slugify(req.slug()))
        .description(req.description())
        .build();
    return toDto(repo.save(cat));
  }

  // @Override
  // @Transactional
  // public ForumCategoryResponse update(UUID id, ForumCategoryUpdateRequest req) {
  //   var cat = repo.findById(id).orElseThrow();
  //   if (req.name() != null) cat.setName(req.name());
  //   if (req.slug() != null) cat.setSlug(SlugUtil.slugify(req.slug()));
  //   if (req.description() != null) cat.setDescription(req.description());
  //   return toDto(repo.save(cat));
  // }
  @Override
@Transactional
public ForumCategoryResponse update(UUID id, ForumCategoryUpdateRequest req) {
    var cat = repo.findById(id).orElseThrow();

    if (req.name() != null) {
        cat.setName(req.name());
        if (req.slug() == null || req.slug().isBlank()) {
            cat.setSlug(SlugUtil.slugify(req.name()));
        }
    }

    if (req.slug() != null && !req.slug().isBlank()) {
        cat.setSlug(SlugUtil.slugify(req.slug()));
    }

    if (req.description() != null) {
        cat.setDescription(req.description());
    }

    return toDto(repo.save(cat));
}

  @Override
  @Transactional
  public void delete(UUID id) { repo.deleteById(id); }

  private ForumCategoryResponse toDto(ForumCategory c) {
    return new ForumCategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getCreatedAt());
  }
}
