package com.english.api.forum.service.impl;

import com.english.api.forum.dto.request.ForumCategoryCreateRequest;
import com.english.api.forum.dto.request.ForumCategoryUpdateRequest;
import com.english.api.forum.dto.response.ForumCategoryResponse;
import com.english.api.forum.model.ForumCategory;
import com.english.api.forum.repository.ForumCategoryRepository;
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
        // Auto-generate unique slug from name
        String baseSlug = SlugUtil.slugify(request.name());
        String uniqueSlug = SlugUtil.ensureUnique(baseSlug, 
            s -> categoryRepo.findBySlug(s).isPresent());
        
        ForumCategory category = ForumCategory.builder()
                .name(request.name())
                .slug(uniqueSlug)
                .description(request.description())
                .build();
        return toDto(categoryRepo.save(category));
    }

    @Override
    @Transactional
    public ForumCategoryResponse update(UUID id, ForumCategoryUpdateRequest request) {
        ForumCategory category = categoryRepo.findById(id).orElseThrow();

        // Update name and regenerate slug if name changed
        if (request.name() != null && !request.name().equals(category.getName())) {
            category.setName(request.name());
            
            // Auto-generate new unique slug from new name
            String baseSlug = SlugUtil.slugify(request.name());
            String uniqueSlug = SlugUtil.ensureUnique(baseSlug, 
                s -> categoryRepo.findBySlug(s).isPresent());
            category.setSlug(uniqueSlug);
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
