package com.english.api.course.service.impl;

import com.english.api.common.exception.DuplicatePositionException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.course.dto.request.CourseModuleRequest;
import com.english.api.course.dto.request.CourseModuleUpdateRequest;
import com.english.api.course.dto.response.CourseModuleResponse;
import com.english.api.course.dto.response.CourseModuleUpdateResponse;
import com.english.api.course.mapper.CourseModuleMapper;
import com.english.api.course.model.Course;
import com.english.api.course.model.CourseModule;
import com.english.api.course.repository.CourseModuleRepository;
import com.english.api.course.repository.CourseRepository;
import com.english.api.course.service.CourseModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by hungpham on 10/4/2025
 */
@Service
@RequiredArgsConstructor
public class CourseModuleServiceImpl implements CourseModuleService {
    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final CourseModuleMapper mapper;

    @Override
    @Transactional
    public List<CourseModuleResponse> create(UUID courseId, List<CourseModuleRequest> requests) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Check duplicate position trong payload
        Set<Integer> seenPositions = new HashSet<>();
        for (CourseModuleRequest req : requests) {
            Integer pos = req.position();
            if (pos != null && !seenPositions.add(pos)) {
                throw new DuplicatePositionException("Duplicate position " + pos + " in request payload");
            }
        }

        // Lấy vị trí lớn nhất hiện tại trong DB
        int currentMax = moduleRepository.findMaxPositionByCourseId(courseId).orElse(0);

        // Gán position (auto tăng nếu thiếu) + check trùng DB
        List<CourseModule> modules = new ArrayList<>();
        int autoPosCounter = currentMax;

        for (CourseModuleRequest req : requests) {
            Integer pos = req.position();
            if (pos == null || pos <= 0) {
                pos = ++autoPosCounter;
            } else {
                if (moduleRepository.existsByCourseIdAndPosition(courseId, pos)) {
                    throw new DuplicatePositionException(
                            String.format("Position %d already exists in course %s", pos, courseId)
                    );
                }
            }

            CourseModule module = mapper.toEntity(req);
            module.setCourse(course);
            module.setPosition(pos);

            modules.add(module);
        }

        moduleRepository.saveAll(modules);

        return modules.stream().map(mapper::toResponse).toList();
    }


    @Override
    public List<CourseModuleResponse> list(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found");
        }
        return moduleRepository.findModulesWithLessonCount(courseId);
    }

    @Override
    public CourseModuleResponse getById(UUID courseId, UUID moduleId) {
        return moduleRepository.findModuleWithLessonCount(courseId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));
    }

    @Override
    @Transactional
    public List<CourseModuleUpdateResponse> update(UUID courseId, List<CourseModuleUpdateRequest> requests) {
        // Check duplicate position trong payload
        Set<Integer> seen = new HashSet<>();
        for (CourseModuleUpdateRequest req : requests) {
            if (!seen.add(req.position())) {
                throw new DuplicatePositionException("Duplicate position " + req.position() + " in request payload");
            }
        }

        // Lấy tất cả modules cần update
        List<UUID> ids = requests.stream().map(CourseModuleUpdateRequest::id).toList();
        List<CourseModule> modules = moduleRepository.findAllById(ids);

        if (modules.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more modules not found");
        }

        // Validate ownership (đảm bảo module thuộc cùng course)
        modules.forEach(m -> {
            if (!m.getCourse().getId().equals(courseId)) {
                throw new ResourceInvalidException("Module " + m.getId() + " does not belong to course " + courseId);
            }
        });

        // Check conflict với module khác cùng course
        for (CourseModuleUpdateRequest req : requests) {
            // ignore nếu module đang giữ cùng position (self-update)
            boolean conflict = moduleRepository.existsByCourseIdAndPosition(courseId, req.position()) &&
                               modules.stream().noneMatch(m ->
                                       m.getId().equals(req.id()) &&
                                       m.getPosition().equals(req.position())
                               );

            if (conflict) {
                throw new DuplicatePositionException(
                        String.format("Position %d already exists in course %s", req.position(), courseId)
                );
            }
        }

        Map<UUID, CourseModuleUpdateRequest> requestMap = requests.stream()
                .collect(Collectors.toMap(CourseModuleUpdateRequest::id, r -> r));

        modules.forEach(m -> {
            CourseModuleUpdateRequest req = requestMap.get(m.getId());
            m.setTitle(req.title());
            m.setPosition(req.position());
        });

        moduleRepository.saveAll(modules);

        return modules.stream().map(mapper::toUpdateResponse).toList();
    }


    @Override
    @Transactional
    public void delete(UUID courseId, UUID moduleId) {
        CourseModule module = moduleRepository.findById(moduleId)
                .filter(m -> m.getCourse().getId().equals(courseId))
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        //  Integer deletedPosition = module.getPosition();
        moduleRepository.delete(module);

        // update position
        // moduleRepository.shiftPositionsAfterDelete(courseId, deletedPosition);
    }
}
