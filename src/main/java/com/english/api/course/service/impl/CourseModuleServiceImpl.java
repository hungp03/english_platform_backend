package com.english.api.course.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.DuplicatePositionException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.common.exception.UnauthorizedException;
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
    public CourseModuleResponse create(UUID courseId, CourseModuleRequest request) {
        // Tìm khóa học
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Integer pos = request.position();

        // Nếu không truyền position hoặc <= 0 → tự tăng từ max hiện tại
        if (pos == null || pos <= 0) {
            int currentMax = moduleRepository.findMaxPositionByCourseId(courseId).orElse(0);
            pos = currentMax + 1;
        } else {
            // Kiểm tra trùng position với module khác trong cùng course
            if (moduleRepository.existsByCourseIdAndPosition(courseId, pos)) {
                throw new DuplicatePositionException(
                        String.format("Position %d already exists in course %s", pos, courseId)
                );
            }
        }

        // Tạo entity
        CourseModule module = mapper.toEntity(request);
        module.setCourse(course);
        module.setPosition(pos);

        moduleRepository.save(module);

        return mapper.toResponse(module);
    }



    @Override
    public List<CourseModuleResponse> list(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found");
        }
        return moduleRepository.findModulesWithLessonCount(courseId);
    }

    @Override
    public List<CourseModuleResponse> listPublished(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found");
        }
        return moduleRepository.findPublishedModulesWithLessonCount(courseId);
    }

    @Override
    public CourseModuleResponse getById(UUID courseId, UUID moduleId) {
        return moduleRepository.findModuleWithLessonCount(courseId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));
    }

    @Override
    @Transactional
    public CourseModuleUpdateResponse update(UUID courseId, CourseModuleUpdateRequest request) {
        // Kiểm tra quyền sở hữu khóa học trước
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        UUID ownerId = courseRepository.findOwnerIdById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!ownerId.equals(currentUserId)) {
            throw new UnauthorizedException("You are not allowed to modify this course.");
        }

        // 2Tìm module cần update và xác nhận thuộc đúng course
        CourseModule module = moduleRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        if (!module.getCourse().getId().equals(courseId)) {
            throw new ResourceInvalidException(
                    "Module " + module.getId() + " does not belong to course " + courseId
            );
        }

        // 3Kiểm tra trùng vị trí trong cùng khóa học (trừ chính nó)
        boolean conflict = moduleRepository.existsByCourseIdAndPosition(courseId, request.position())
                           && !Objects.equals(module.getPosition(), request.position());
        if (conflict) {
            throw new DuplicatePositionException(
                    String.format("Position %d already exists in course %s", request.position(), courseId)
            );
        }

        // Cập nhật dữ liệu module
        module.setTitle(request.title());
        module.setPosition(request.position());

        moduleRepository.save(module);

        return mapper.toUpdateResponse(module);
    }




    @Override
    @Transactional
    public void delete(UUID courseId, UUID moduleId) {
        // Kiểm tra quyền sở hữu
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        UUID ownerId = courseRepository.findOwnerIdById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!ownerId.equals(currentUserId)) {
            throw new UnauthorizedException("You are not allowed to modify this course.");
        }

        // Xác nhận module thuộc đúng course
        CourseModule module = moduleRepository.findById(moduleId)
                .filter(m -> m.getCourse().getId().equals(courseId))
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        // Xóa module
        moduleRepository.delete(module);

        // Cập nhật vị trí
        // moduleRepository.shiftPositionsAfterDelete(courseId, module.getPosition());
    }

    @Override
    @Transactional
    public CourseModuleResponse publish(UUID courseId, UUID moduleId, boolean publish) {
        // Kiểm tra quyền sở hữu
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        UUID ownerId = courseRepository.findOwnerIdById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!ownerId.equals(currentUserId)) {
            throw new UnauthorizedException("You are not allowed to modify this course.");
        }

        // Tìm module
        CourseModule module = moduleRepository.findById(moduleId)
                .filter(m -> m.getCourse().getId().equals(courseId))
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        // Cập nhật trạng thái publish
        module.setPublished(publish);
        moduleRepository.save(module);

        return mapper.toResponse(module);
    }

}
