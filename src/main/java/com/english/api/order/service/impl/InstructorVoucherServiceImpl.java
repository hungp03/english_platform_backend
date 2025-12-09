package com.english.api.order.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.course.model.Course;
import com.english.api.course.repository.CourseRepository;
import com.english.api.order.dto.request.ApplyVoucherDirectRequest;
import com.english.api.order.dto.request.ApplyVoucherRequest;
import com.english.api.order.dto.request.CreateVoucherRequest;
import com.english.api.order.dto.request.UpdateVoucherRequest;
import com.english.api.order.dto.response.CartCheckoutResponse;
import com.english.api.order.dto.response.VoucherApplyResponse;
import com.english.api.order.dto.response.VoucherResponse;
import com.english.api.order.mapper.VoucherMapper;
import com.english.api.order.model.InstructorVoucher;
import com.english.api.order.model.enums.VoucherDiscountType;
import com.english.api.order.model.enums.VoucherScope;
import com.english.api.order.model.enums.VoucherStatus;
import com.english.api.order.repository.CartItemRepository;
import com.english.api.order.repository.InstructorVoucherRepository;
import com.english.api.order.repository.InstructorVoucherUsageRepository;
import com.english.api.order.service.InstructorVoucherService;
import com.english.api.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorVoucherServiceImpl implements InstructorVoucherService {

    private final InstructorVoucherRepository voucherRepository;
    private final InstructorVoucherUsageRepository voucherUsageRepository;
    private final CourseRepository courseRepository;
    private final CartItemRepository cartItemRepository;
    private final VoucherMapper voucherMapper;

    @Override
    @Transactional
    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        UUID instructorId = SecurityUtil.getCurrentUserId();
        log.info("Instructor {} creating voucher with code: {}", instructorId, request.code());

        // Validate voucher code uniqueness
        if (voucherRepository.existsByCode(request.code().toUpperCase())) {
            throw new ResourceAlreadyExistsException("Mã voucher đã tồn tại");
        }

        // Validate date range
        if (request.endDate().isBefore(request.startDate())) {
            throw new ResourceInvalidException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        // Validate discount value for percentage
        if (request.discountType() == VoucherDiscountType.PERCENTAGE && 
            request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ResourceInvalidException("Phần trăm giảm giá không được vượt quá 100%");
        }

        // Validate courses if scope is SPECIFIC_COURSES
        Set<Course> applicableCourses = new HashSet<>();
        if (request.scope() == VoucherScope.SPECIFIC_COURSES) {
            if (request.courseIds() == null || request.courseIds().isEmpty()) {
                throw new ResourceInvalidException("Phải chọn ít nhất một khóa học khi phạm vi là khóa học cụ thể");
            }
            applicableCourses = validateAndGetCourses(request.courseIds(), instructorId);
        }

        InstructorVoucher voucher = InstructorVoucher.builder()
                .code(request.code().toUpperCase())
                .instructor(User.builder().id(instructorId).build())
                .scope(request.scope())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .maxDiscountAmount(request.maxDiscountAmount())
                .minOrderAmount(request.minOrderAmount() != null ? request.minOrderAmount() : BigDecimal.ZERO)
                .usageLimit(request.usageLimit())
                .usagePerUser(request.usagePerUser() != null ? request.usagePerUser() : 1)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(VoucherStatus.ACTIVE)
                .applicableCourses(applicableCourses)
                .build();

        voucher = voucherRepository.save(voucher);
        log.info("Created voucher {} with ID: {}", voucher.getCode(), voucher.getId());

        return voucherMapper.toResponse(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(UUID voucherId, UpdateVoucherRequest request) {
        UUID instructorId = SecurityUtil.getCurrentUserId();
        
        InstructorVoucher voucher = voucherRepository.findByIdAndInstructorIdWithCourses(voucherId, instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));

        // Update fields if provided
        if (request.scope() != null) {
            voucher.setScope(request.scope());
        }
        if (request.discountType() != null) {
            voucher.setDiscountType(request.discountType());
        }
        if (request.discountValue() != null) {
            if (voucher.getDiscountType() == VoucherDiscountType.PERCENTAGE && 
                request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new ResourceInvalidException("Phần trăm giảm giá không được vượt quá 100%");
            }
            voucher.setDiscountValue(request.discountValue());
        }
        if (request.maxDiscountAmount() != null) {
            voucher.setMaxDiscountAmount(request.maxDiscountAmount());
        }
        if (request.minOrderAmount() != null) {
            voucher.setMinOrderAmount(request.minOrderAmount());
        }
        if (request.usageLimit() != null) {
            voucher.setUsageLimit(request.usageLimit());
        }
        if (request.usagePerUser() != null) {
            voucher.setUsagePerUser(request.usagePerUser());
        }
        if (request.startDate() != null) {
            voucher.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            voucher.setEndDate(request.endDate());
        }
        if (request.status() != null) {
            voucher.setStatus(request.status());
        }

        // Validate date range
        if (voucher.getEndDate().isBefore(voucher.getStartDate())) {
            throw new ResourceInvalidException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        // Update applicable courses if provided
        if (request.courseIds() != null) {
            if (voucher.getScope() == VoucherScope.SPECIFIC_COURSES && request.courseIds().isEmpty()) {
                throw new ResourceInvalidException("Phải chọn ít nhất một khóa học khi phạm vi là khóa học cụ thể");
            }
            Set<Course> courses = validateAndGetCourses(request.courseIds(), instructorId);
            voucher.getApplicableCourses().clear();
            voucher.getApplicableCourses().addAll(courses);
        }

        voucher = voucherRepository.save(voucher);
        log.info("Updated voucher {} by instructor {}", voucherId, instructorId);

        return voucherMapper.toResponse(voucher);
    }

    @Override
    @Transactional
    public void deleteVoucher(UUID voucherId) {
        UUID instructorId = SecurityUtil.getCurrentUserId();
        
        InstructorVoucher voucher = voucherRepository.findByIdAndInstructorIdWithCourses(voucherId, instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));

        voucher.setStatus(VoucherStatus.INACTIVE);
        voucherRepository.save(voucher);
        log.info("Deleted (deactivated) voucher {} by instructor {}", voucherId, instructorId);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(UUID voucherId) {
        UUID instructorId = SecurityUtil.getCurrentUserId();
        
        InstructorVoucher voucher = voucherRepository.findByIdAndInstructorIdWithCourses(voucherId, instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));

        return voucherMapper.toResponse(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse getMyVouchers(VoucherStatus status, Pageable pageable) {
        UUID instructorId = SecurityUtil.getCurrentUserId();
        
        Page<InstructorVoucher> voucherPage = voucherRepository.findByInstructorIdAndStatus(
                instructorId, status, pageable);
        
        Page<VoucherResponse> responsePage = voucherPage.map(voucherMapper::toResponse);
        
        return PaginationResponse.from(responsePage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherApplyResponse applyVoucherToCart(ApplyVoucherRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        String code = request.code().toUpperCase();
        
        // Get cart items
        List<CartCheckoutResponse> cartItems = cartItemRepository.findCoursesForCheckoutByUserId(userId);
        if (cartItems.isEmpty()) {
            return createInvalidResponse(code, "Giỏ hàng trống");
        }

        // Find valid voucher
        Optional<InstructorVoucher> voucherOpt = voucherRepository.findValidVoucherByCode(
                code, OffsetDateTime.now());
        
        if (voucherOpt.isEmpty()) {
            return createInvalidResponse(code, "Mã voucher không hợp lệ hoặc đã hết hạn");
        }

        InstructorVoucher voucher = voucherOpt.get();
        
        // Check usage per user
        int userUsageCount = voucherUsageRepository.countByVoucherIdAndUserId(voucher.getId(), userId);
        if (userUsageCount >= voucher.getUsagePerUser()) {
            return createInvalidResponse(code, "Bạn đã sử dụng hết số lần cho phép với voucher này");
        }

        // Calculate discount for applicable courses
        return calculateVoucherDiscount(voucher, cartItems);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherApplyResponse validateVoucher(String code) {
        UUID userId = SecurityUtil.getCurrentUserId();
        code = code.toUpperCase();
        
        Optional<InstructorVoucher> voucherOpt = voucherRepository.findValidVoucherByCode(
                code, OffsetDateTime.now());
        
        if (voucherOpt.isEmpty()) {
            return createInvalidResponse(code, "Mã voucher không hợp lệ hoặc đã hết hạn");
        }

        InstructorVoucher voucher = voucherOpt.get();
        
        // Check usage per user
        int userUsageCount = voucherUsageRepository.countByVoucherIdAndUserId(voucher.getId(), userId);
        if (userUsageCount >= voucher.getUsagePerUser()) {
            return createInvalidResponse(code, "Bạn đã sử dụng hết số lần cho phép với voucher này");
        }

        return new VoucherApplyResponse(
                voucher.getCode(),
                voucher.getDiscountType(),
                voucher.getDiscountValue(),
                voucher.getMaxDiscountAmount(),
                BigDecimal.ZERO,
                List.of(),
                true,
                "Voucher hợp lệ"
        );
    }

    private Set<Course> validateAndGetCourses(Set<UUID> courseIds, UUID instructorId) {
        List<Course> courses = courseRepository.findAllById(courseIds);
        
        // Verify all courses belong to this instructor
        for (Course course : courses) {
            if (!course.getCreatedBy().getId().equals(instructorId)) {
                throw new AccessDeniedException("Bạn không có quyền tạo voucher cho khóa học không thuộc về bạn");
            }
        }

        if (courses.size() != courseIds.size()) {
            throw new ResourceNotFoundException("Một số khóa học không tồn tại");
        }

        return new HashSet<>(courses);
    }

    private VoucherApplyResponse calculateVoucherDiscount(InstructorVoucher voucher, List<CartCheckoutResponse> cartItems) {
        UUID instructorId = voucher.getInstructor().getId();
        
        // Filter applicable courses
        List<CartCheckoutResponse> applicableCourses = cartItems.stream()
                .filter(item -> item.instructorId().equals(instructorId))
                .filter(item -> {
                    if (voucher.getScope() == VoucherScope.ALL_INSTRUCTOR_COURSES) {
                        return true;
                    }
                    return voucher.getApplicableCourses().stream()
                            .anyMatch(c -> c.getId().equals(item.courseId()));
                })
                .toList();

        if (applicableCourses.isEmpty()) {
            return createInvalidResponse(voucher.getCode(), 
                    "Không có khóa học nào trong giỏ hàng áp dụng được voucher này");
        }

        // Calculate total for applicable courses
        BigDecimal applicableTotal = applicableCourses.stream()
                .map(c -> BigDecimal.valueOf(c.priceCents()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Check minimum order amount
        if (voucher.getMinOrderAmount() != null && 
            applicableTotal.compareTo(voucher.getMinOrderAmount()) < 0) {
            return createInvalidResponse(voucher.getCode(), 
                    String.format("Tổng giá trị đơn hàng phải từ %,d đ trở lên", 
                            voucher.getMinOrderAmount().longValue()));
        }

        // Calculate discount for each course
        List<VoucherApplyResponse.CourseDiscountDetail> courseDiscounts = new ArrayList<>();
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (CartCheckoutResponse course : applicableCourses) {
            BigDecimal originalPrice = BigDecimal.valueOf(course.priceCents());
            BigDecimal discount;

            if (voucher.getDiscountType() == VoucherDiscountType.PERCENTAGE) {
                discount = originalPrice.multiply(voucher.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
                
                // Apply max discount if set
                if (voucher.getMaxDiscountAmount() != null) {
                    if (discount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                        discount = voucher.getMaxDiscountAmount();
                    }
                }
            } else {
                // FIXED_AMOUNT
                discount = voucher.getDiscountValue();
                if (discount.compareTo(originalPrice) > 0) {
                    discount = originalPrice;
                }
            }

            BigDecimal finalPrice = originalPrice.subtract(discount);
            totalDiscount = totalDiscount.add(discount);

            courseDiscounts.add(new VoucherApplyResponse.CourseDiscountDetail(
                    course.courseId(),
                    course.title(),
                    originalPrice,
                    discount,
                    finalPrice
            ));
        }

        return new VoucherApplyResponse(
                voucher.getCode(),
                voucher.getDiscountType(),
                voucher.getDiscountValue(),
                voucher.getMaxDiscountAmount(),
                totalDiscount,
                courseDiscounts,
                true,
                "Áp dụng voucher thành công"
        );
    }

    private VoucherApplyResponse createInvalidResponse(String code, String message) {
        return new VoucherApplyResponse(
                code,
                null,
                null,
                null,
                BigDecimal.ZERO,
                List.of(),
                false,
                message
        );
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherApplyResponse applyVoucherToCourseIds(String code, List<UUID> courseIds) {
        UUID userId = SecurityUtil.getCurrentUserId();
        String normalizedCode = code.toUpperCase().trim();

        log.info("User {} applying voucher {} to courses {}", userId, normalizedCode, courseIds);

        if (courseIds == null || courseIds.isEmpty()) {
            return createInvalidResponse(normalizedCode, "Không có khóa học nào để áp dụng voucher");
        }

        // Find valid voucher
        Optional<InstructorVoucher> voucherOpt = voucherRepository.findValidVoucherByCode(normalizedCode, OffsetDateTime.now());
        if (voucherOpt.isEmpty()) {
            return createInvalidResponse(normalizedCode, "Mã voucher không hợp lệ hoặc đã hết hạn");
        }

        InstructorVoucher voucher = voucherOpt.get();

        // Check usage per user
        int userUsageCount = voucherUsageRepository.countByVoucherIdAndUserId(voucher.getId(), userId);
        if (userUsageCount >= voucher.getUsagePerUser()) {
            return createInvalidResponse(normalizedCode, "Bạn đã sử dụng hết số lần cho phép với voucher này");
        }

        // Get courses and convert to CartCheckoutResponse format
        List<Course> courses = courseRepository.findAllById(courseIds);
        List<CartCheckoutResponse> cartItems = courses.stream()
                .map(course -> new CartCheckoutResponse(
                        course.getId(),    // id (cart item id, use course id as placeholder)
                        course.getId(),    // courseId
                        course.getTitle(),
                        course.getThumbnail(),
                        course.getPriceCents(),
                        course.getCurrency(),
                        course.getCreatedBy().getId()
                ))
                .toList();

        if (cartItems.isEmpty()) {
            return createInvalidResponse(normalizedCode, "Không tìm thấy khóa học");
        }

        // Calculate discount for applicable courses
        return calculateVoucherDiscount(voucher, cartItems);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherApplyResponse applyVoucherToCourse(ApplyVoucherDirectRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        String code = request.code().toUpperCase().trim();
        UUID courseId = request.courseId();

        log.info("User {} applying voucher {} to course {}", userId, code, courseId);

        // Find and validate voucher
        Optional<InstructorVoucher> voucherOpt = voucherRepository.findValidVoucherByCode(code, OffsetDateTime.now());
        if (voucherOpt.isEmpty()) {
            return createInvalidResponse(code, "Voucher không tồn tại hoặc đã hết hạn");
        }

        InstructorVoucher voucher = voucherOpt.get();

        // Check usage limit
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            return createInvalidResponse(code, "Voucher đã hết lượt sử dụng");
        }

        // Check usage per user
        int userUsageCount = voucherUsageRepository.countByVoucherIdAndUserId(voucher.getId(), userId);
        if (userUsageCount >= voucher.getUsagePerUser()) {
            return createInvalidResponse(code, "Bạn đã sử dụng hết số lần cho phép với voucher này");
        }

        // Get course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));

        // Check if course belongs to voucher's instructor
        UUID instructorId = voucher.getInstructor().getId();
        if (!course.getCreatedBy().getId().equals(instructorId)) {
            return createInvalidResponse(code, "Voucher không áp dụng cho khóa học này");
        }

        // Check if voucher scope includes this course
        if (voucher.getScope() == VoucherScope.SPECIFIC_COURSES) {
            boolean isCourseInVoucher = voucher.getApplicableCourses().stream()
                    .anyMatch(c -> c.getId().equals(courseId));
            if (!isCourseInVoucher) {
                return createInvalidResponse(code, "Voucher không áp dụng cho khóa học này");
            }
        }

        // Calculate discount for this course
        BigDecimal coursePrice = BigDecimal.valueOf(course.getPriceCents());

        // Check min order amount
        if (voucher.getMinOrderAmount() != null && 
                coursePrice.compareTo(voucher.getMinOrderAmount()) < 0) {
            return createInvalidResponse(code, 
                    String.format("Giá trị đơn hàng tối thiểu là %,dđ", voucher.getMinOrderAmount().longValue()));
        }

        // Calculate discount
        BigDecimal discountAmount;
        if (voucher.getDiscountType() == VoucherDiscountType.PERCENTAGE) {
            discountAmount = coursePrice
                    .multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
            // Apply max discount cap
            if (voucher.getMaxDiscountAmount() != null) {
                if (discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                    discountAmount = voucher.getMaxDiscountAmount();
                }
            }
        } else {
            discountAmount = voucher.getDiscountValue();
            // Don't exceed course price
            if (discountAmount.compareTo(coursePrice) > 0) {
                discountAmount = coursePrice;
            }
        }

        BigDecimal finalPrice = coursePrice.subtract(discountAmount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        VoucherApplyResponse.CourseDiscountDetail courseDiscount = new VoucherApplyResponse.CourseDiscountDetail(
                course.getId(),
                course.getTitle(),
                coursePrice,
                discountAmount,
                finalPrice
        );

        return new VoucherApplyResponse(
                voucher.getCode(),
                voucher.getDiscountType(),
                voucher.getDiscountValue(),
                voucher.getMaxDiscountAmount(),
                discountAmount,
                List.of(courseDiscount),
                true,
                "Áp dụng voucher thành công"
        );
    }
}
