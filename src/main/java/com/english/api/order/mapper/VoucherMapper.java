package com.english.api.order.mapper;

import com.english.api.course.model.Course;
import com.english.api.order.dto.response.VoucherResponse;
import com.english.api.order.model.InstructorVoucher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VoucherMapper {

    public VoucherResponse toResponse(InstructorVoucher voucher) {
        List<VoucherResponse.VoucherCourseResponse> courses = voucher.getApplicableCourses().stream()
                .map(this::toCourseResponse)
                .toList();

        return new VoucherResponse(
                voucher.getId(),
                voucher.getCode(),
                voucher.getScope(),
                voucher.getDiscountType(),
                voucher.getDiscountValue(),
                voucher.getMaxDiscountAmount(),
                voucher.getMinOrderAmount(),
                voucher.getUsageLimit(),
                voucher.getUsagePerUser(),
                voucher.getUsedCount(),
                voucher.getStartDate(),
                voucher.getEndDate(),
                voucher.getStatus(),
                courses,
                voucher.getCreatedAt()
        );
    }

    private VoucherResponse.VoucherCourseResponse toCourseResponse(Course course) {
        return new VoucherResponse.VoucherCourseResponse(
                course.getId(),
                course.getTitle(),
                course.getSlug(),
                course.getThumbnail()
        );
    }
}
