package com.english.api.enrollment.mapper;

import com.english.api.enrollment.dto.response.EnrollmentResponse;
import com.english.api.enrollment.model.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper for Enrollment entity
 * Created by hungpham on 11/03/2025
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EnrollmentMapper {

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    @Mapping(target = "courseSlug", source = "course.slug")
    @Mapping(target = "courseThumbnail", source = "course.thumbnail")
    EnrollmentResponse toEnrollmentResponse(Enrollment enrollment);

    List<EnrollmentResponse> toEnrollmentResponses(List<Enrollment> enrollments);
}
