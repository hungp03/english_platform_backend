
package com.english.api.admin.service.impl;

import com.english.api.admin.dto.response.*;
import com.english.api.admin.repository.DashboardStatsRepositoryCustom;
import com.english.api.admin.service.AdminOverviewService;
import com.english.api.enrollment.repository.EnrollmentRepository;
import com.english.api.forum.repository.ForumReportRepository;
import com.english.api.order.model.enums.OrderStatus;
import com.english.api.order.repository.OrderRepository;
import com.english.api.user.model.InstructorRequest;
import com.english.api.user.repository.InstructorRequestRepository;
import com.english.api.user.repository.UserRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOverviewServiceImpl implements AdminOverviewService {

    // === REPOSITORIES ===
    private final UserRepository userRepository;
    private final InstructorRequestRepository instructorRequestRepository;
    private final OrderRepository orderRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ForumReportRepository forumReportRepository;
    private final DashboardStatsRepositoryCustom dashboardStatsRepository;
    
    // === TEMPLATE ENGINE ===
    private final SpringTemplateEngine templateEngine;

    // =========================================================================
    // 1. DASHBOARD SUMMARY
    // =========================================================================
    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        ZoneId utc = ZoneId.of("UTC");
        OffsetDateTime now = OffsetDateTime.now(utc);
        OffsetDateTime weekAgo = now.minusWeeks(1);
        Instant sevenDaysAgo = now.minusDays(7).toInstant();
        Instant threeDaysAgo = now.minusDays(3).toInstant();
        YearMonth current = YearMonth.now(utc);
        OffsetDateTime startOfMonth = current.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfMonth = current.atEndOfMonth().atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
        YearMonth prev = current.minusMonths(1);
        OffsetDateTime startPrevMonth = prev.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endPrevMonth = prev.atEndOfMonth().atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        Map<String, Long> stats = dashboardStatsRepository.getAllDashboardStats(
            weekAgo, sevenDaysAgo, threeDaysAgo,
            startOfMonth, endOfMonth, startPrevMonth, endPrevMonth
        );

        long revenueThisMonth = get(stats, "REVENUE_THIS_MONTH");
        long ordersPaid = get(stats, "ORDERS_PAID");
        BigDecimal aov = ordersPaid > 0
            ? BigDecimal.valueOf(revenueThisMonth).divide(BigDecimal.valueOf(ordersPaid), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        return DashboardSummaryResponse.builder()
            .users(DashboardSummaryResponse.UserStats.builder()
                .total(get(stats, "USERS_TOTAL"))
                .active(get(stats, "USERS_ACTIVE"))
                .verified(get(stats, "USERS_VERIFIED"))
                .inactive(get(stats, "USERS_TOTAL") - get(stats, "USERS_ACTIVE"))
                .weekGrowth(get(stats, "USERS_WEEK_GROWTH"))
                .activePercentage(pct(get(stats, "USERS_ACTIVE"), get(stats, "USERS_TOTAL")))
                .verifiedPercentage(pct(get(stats, "USERS_VERIFIED"), get(stats, "USERS_TOTAL")))
                .build())
            .instructors(DashboardSummaryResponse.InstructorStats.builder()
                .totalInstructors(get(stats, "INSTRUCTORS_TOTAL"))
                .pendingRequests(get(stats, "INSTRUCTOR_REQUESTS_PENDING"))
                .pendingOver7Days(get(stats, "INSTRUCTOR_REQUESTS_OVER_7D"))
                .pending3To7Days(get(stats, "INSTRUCTOR_REQUESTS_3_7D"))
                .pendingUnder3Days(get(stats, "INSTRUCTOR_REQUESTS_UNDER_3D"))
                .build())
            .courses(DashboardSummaryResponse.CourseStats.builder()
                .totalCourses(get(stats, "COURSES_TOTAL"))
                .published(get(stats, "COURSES_PUBLISHED"))
                .draft(get(stats, "COURSES_DRAFT"))
                .archived(get(stats, "COURSES_REJECTED"))
                .totalModules(get(stats, "COURSE_MODULES_TOTAL"))
                .totalLessons(get(stats, "LESSONS_TOTAL"))
                .freeLessons(get(stats, "LESSONS_FREE"))
                .publishedPercentage(pct(get(stats, "COURSES_PUBLISHED"), get(stats, "COURSES_TOTAL")))
                .freeLessonsPercentage(pct(get(stats, "LESSONS_FREE"), get(stats, "LESSONS_TOTAL")))
                .build())
            .quizzes(DashboardSummaryResponse.QuizStats.builder()
                .totalQuizzes(get(stats, "QUIZZES_TOTAL"))
                .totalQuestions(get(stats, "QUESTIONS_TOTAL"))
                .byReading(get(stats, "QUIZZES_READING"))
                .byListening(get(stats, "QUIZZES_LISTENING"))
                .byWriting(get(stats, "QUIZZES_WRITING"))
                .bySpeaking(get(stats, "QUIZZES_SPEAKING"))
                .published(get(stats, "QUIZZES_PUBLISHED"))
                .draft(get(stats, "QUIZZES_DRAFT"))
                .build())
            .revenue(DashboardSummaryResponse.RevenueStats.builder()
                .totalCentsThisMonth(revenueThisMonth)
                .growthPercentage(growth(revenueThisMonth, get(stats, "REVENUE_PREV_MONTH")))
                .totalCentsVND(get(stats, "REVENUE_VND_TOTAL"))
                .build())
            .orders(DashboardSummaryResponse.OrderStats.builder()
                .totalOrdersThisMonth(get(stats, "ORDERS_THIS_MONTH"))
                .completed(get(stats, "ORDERS_PAID"))
                .pending(get(stats, "ORDERS_PENDING"))
                .cancelled(get(stats, "ORDERS_CANCELLED"))
                .unpaidCarts(get(stats, "CART_ITEMS_UNPAID"))
                .completedPercentage(pct(get(stats, "ORDERS_PAID"), get(stats, "ORDERS_THIS_MONTH")))
                .averageOrderValue(aov)
                .build())
            .payments(DashboardSummaryResponse.PaymentStats.builder()
                .totalPayments(get(stats, "PAYMENTS_TOTAL"))
                .byPayPal(get(stats, "PAYMENTS_PAYPAL"))
                .byPayOS(get(stats, "PAYMENTS_PAYOS"))
                .succeeded(get(stats, "PAYMENTS_SUCCESS"))
                .failed(get(stats, "PAYMENTS_FAILED"))
                .refunded(get(stats, "PAYMENTS_REFUNDED"))
                .successRate(pct(get(stats, "PAYMENTS_SUCCESS"), get(stats, "PAYMENTS_TOTAL")))
                .payPalPercentage(pct(get(stats, "PAYMENTS_PAYPAL"), get(stats, "PAYMENTS_TOTAL")))
                .payOSPercentage(pct(get(stats, "PAYMENTS_PAYOS"), get(stats, "PAYMENTS_TOTAL")))
                .build())
            .learning(DashboardSummaryResponse.LearningStats.builder()
                .totalEnrollments(get(stats, "ENROLLMENTS_TOTAL"))
                .completed(get(stats, "ENROLLMENTS_COMPLETED"))
                .suspended(get(stats, "ENROLLMENTS_CANCELLED"))
                .averageProgress(
                    stats.containsKey("ENROLLMENTS_AVG_PROGRESS") 
                        ? BigDecimal.valueOf(stats.get("ENROLLMENTS_AVG_PROGRESS")).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO
                )
                .totalAttempts(get(stats, "QUIZ_ATTEMPTS_TOTAL"))
                .build())
            .content(DashboardSummaryResponse.ContentStats.builder()
                .totalBlogPosts(get(stats, "BLOG_POSTS_TOTAL"))
                .publishedPosts(get(stats, "BLOG_POSTS_PUBLISHED"))
                .draftPosts(get(stats, "BLOG_POSTS_DRAFT"))
                .totalComments(get(stats, "BLOG_COMMENTS_TOTAL"))
                .totalThreads(get(stats, "FORUM_THREADS_TOTAL"))
                .totalForumPosts(get(stats, "FORUM_POSTS_TOTAL"))
                .totalViews(get(stats, "FORUM_VIEWS_TOTAL"))
                .lockedThreads(get(stats, "FORUM_THREADS_LOCKED"))
                .build())
            .build();
    }

    private Long get(Map<String, Long> stats, String key) {
        return stats.getOrDefault(key, 0L);
    }

    private BigDecimal pct(long part, long total) {
        return total == 0 ? BigDecimal.ZERO :
            BigDecimal.valueOf(part * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal growth(long cur, long prev) {
        if (prev == 0) return cur > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        return BigDecimal.valueOf((cur - prev) * 100.0 / prev).setScale(2, RoundingMode.HALF_UP);
    }

    // =========================================================================
    // 2. PENDING ACTIONS
    // =========================================================================
    @Override
    public PendingActionsResponse getPendingActions() {
        Long totalPendingRequest = instructorRequestRepository.countByStatus(InstructorRequest.Status.PENDING);
        Long totalUnsolveReport = forumReportRepository.countByResolvedAtIsNull();
        Long totalPendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        return new PendingActionsResponse(totalPendingRequest, totalUnsolveReport, totalPendingOrders);
    }

    // =========================================================================
    // 3. CHARTS
    // =========================================================================
    @Override
    public UserGrowthResponse getUserGrowthChart(int months) {
        if (months > 24) months = 24;
        Instant start = Instant.now().minus(Duration.ofDays(months * 30L));
        List<Object[]> data = userRepository.getUserGrowthByMonth(start);

        Map<String, UserGrowthResponse.MonthlyData> monthMap = new LinkedHashMap<>();
        LocalDate current = LocalDate.now().withDayOfMonth(1).minusMonths(months - 1);
        LocalDate end = LocalDate.now().withDayOfMonth(1);
        while (!current.isAfter(end)) {
            String key = current.format(DateTimeFormatter.ofPattern("MM-yyyy"));
            monthMap.put(key, new UserGrowthResponse.MonthlyData(key, 0L, 0L));
            current = current.plusMonths(1);
        }

        for (Object[] row : data) {
            String monthStr = ((java.sql.Date) row[0]).toLocalDate().withDayOfMonth(1)
                    .format(DateTimeFormatter.ofPattern("MM-yyyy"));
            Long newUsers = (Long) row[1];
            Long activeUsers = (Long) row[2];
            monthMap.put(monthStr, new UserGrowthResponse.MonthlyData(monthStr, newUsers, activeUsers));
        }
        return new UserGrowthResponse(new ArrayList<>(monthMap.values()));
    }

    @Override
    public RevenueChartResponse getRevenueChart(int months) {
        if (months > 12) months = 12;
        OffsetDateTime startDate = OffsetDateTime.now().minusMonths(months).withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);
        List<Object[]> data = orderRepository.getRevenueByMonth(startDate);

        Map<String, RevenueChartResponse.MonthlyRevenue> monthMap = new LinkedHashMap<>();
        LocalDate current = LocalDate.now().withDayOfMonth(1).minusMonths(months - 1);
        LocalDate end = LocalDate.now().withDayOfMonth(1);
        while (!current.isAfter(end)) {
            String key = current.format(DateTimeFormatter.ofPattern("MM-yyyy"));
            monthMap.put(key, new RevenueChartResponse.MonthlyRevenue(key, 0L, 0L, 0L, BigDecimal.ZERO));
            current = current.plusMonths(1);
        }

        for (Object[] row : data) {
            OffsetDateTime dateTime = (OffsetDateTime) row[0];
            String monthStr = dateTime.toLocalDate().withDayOfMonth(1)
                    .format(DateTimeFormatter.ofPattern("MM-yyyy"));
            Long vnd = (Long) row[1];
            Long usd = (Long) row[2];
            Long orders = (Long) row[3];
            BigDecimal rate = new BigDecimal("26349");
            BigDecimal totalVnd = BigDecimal.valueOf(vnd)
                    .add(BigDecimal.valueOf(usd).multiply(rate));

            BigDecimal avg = orders > 0
                    ? totalVnd.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            monthMap.put(monthStr, new RevenueChartResponse.MonthlyRevenue(monthStr, vnd, usd, orders, avg));
        }
        return new RevenueChartResponse(new ArrayList<>(monthMap.values()));
    }

    @Override
    public EnrollmentChartResponse getEnrollmentChart(int months) {
        if (months > 12) months = 12;
        OffsetDateTime startDate = OffsetDateTime.now().minusMonths(months).withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);
        List<Object[]> data = enrollmentRepository.getEnrollmentsByMonth(startDate);

        Map<String, EnrollmentChartResponse.MonthlyEnrollment> monthMap = new LinkedHashMap<>();
        LocalDate current = LocalDate.now().withDayOfMonth(1).minusMonths(months - 1);
        LocalDate end = LocalDate.now().withDayOfMonth(1);
        while (!current.isAfter(end)) {
            String key = current.format(DateTimeFormatter.ofPattern("MM-yyyy"));
            monthMap.put(key, new EnrollmentChartResponse.MonthlyEnrollment(key, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO));
            current = current.plusMonths(1);
        }

        for (Object[] row : data) {
            OffsetDateTime dateTime = (OffsetDateTime) row[0];
            String monthStr = dateTime.toLocalDate().withDayOfMonth(1)
                    .format(DateTimeFormatter.ofPattern("MM-yyyy"));
            Long newEnroll = (Long) row[1];
            Long completed = (Long) row[2];
            Double avgProgress = (Double) row[3];
            BigDecimal completionRate = newEnroll > 0
                    ? BigDecimal.valueOf(completed * 100.0 / newEnroll).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            monthMap.put(monthStr, new EnrollmentChartResponse.MonthlyEnrollment(
                monthStr, newEnroll, completed, completionRate,
                BigDecimal.valueOf(avgProgress != null ? avgProgress : 0).setScale(2, RoundingMode.HALF_UP)
            ));
        }
        return new EnrollmentChartResponse(new ArrayList<>(monthMap.values()));
    }

    // =========================================================================
    // 4. TOP PERFORMERS
    // =========================================================================
    @Override
    public TopPerformersResponse getTopCourses(int limit) {
        if (limit > 50) limit = 50;
        List<Object[]> data = enrollmentRepository.findTopCoursesByEnrollmentCount(PageRequest.of(0, limit));
        List<TopPerformersResponse.TopCourse> list = data.stream().map(row -> {
            UUID courseId = (UUID) row[0];
            String title = (String) row[1];
            String slug = (String) row[2];
            String thumbnail = (String) row[3];
            String instructorName = (String) row[4];
            UUID instructorId = (UUID) row[5];
            Long enrollmentCount = (Long) row[6];
            Long completionCount = (Long) row[7];
            Long priceCents = (Long) row[9];
            
            BigDecimal completionRate = enrollmentCount > 0
                    ? BigDecimal.valueOf(completionCount * 100.0 / enrollmentCount).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            
            return new TopPerformersResponse.TopCourse(
                    courseId, title, slug, thumbnail, instructorName, instructorId,
                    enrollmentCount, completionCount, completionRate, null,
                    priceCents != null ? priceCents * enrollmentCount : 0L, "VND", 0
            );
        }).collect(Collectors.toList());
        
        for (int i = 0; i < list.size(); i++) {
            list.set(i, new TopPerformersResponse.TopCourse(
                list.get(i).id(), list.get(i).title(), list.get(i).slug(), list.get(i).thumbnail(),
                list.get(i).instructorName(), list.get(i).instructorId(), list.get(i).enrollmentCount(),
                list.get(i).completionCount(), list.get(i).completionRate(), list.get(i).averageRating(),
                list.get(i).totalRevenueCents(), list.get(i).currency(), i + 1
            ));
        }
        
        return new TopPerformersResponse(list, null, null);
    }

    @Override
    public TopPerformersResponse getTopInstructors(int limit) {
        if (limit > 50) limit = 50;
        List<Object[]> data = orderRepository.findTopInstructorsByRevenue(PageRequest.of(0, limit));
        List<TopPerformersResponse.TopInstructor> list = data.stream()
                .map(row -> {
                UUID instructorId = (UUID) row[0];
                String fullName = (String) row[1];
                String email = (String) row[2];
                String avatarUrl = (String) row[3];
                Long totalRevenueCents = (Long) row[4];
                Long totalCourses = (Long) row[5];
                Long totalEnrollments = (Long) row[6];

                return new TopPerformersResponse.TopInstructor(
                        instructorId, fullName, email, avatarUrl, totalCourses, null,
                        totalEnrollments, null, null, totalRevenueCents, 0
                );
                })
                .collect(Collectors.toList());

        for (int i = 0; i < list.size(); i++) {
            TopPerformersResponse.TopInstructor old = list.get(i);
            list.set(i, new TopPerformersResponse.TopInstructor(
                old.id(), old.fullName(), old.email(), old.avatarUrl(), old.totalCourses(),
                old.publishedCourses(), old.totalEnrollments(), old.totalStudents(),
                old.averageRating(), old.totalRevenueCents(), i + 1
            ));
        }
        return new TopPerformersResponse(null, list, null);
    }

    @Override
    public TopPerformersResponse getTopRevenueCourses(int limit) {
        if (limit > 50) limit = 50;
        List<Object[]> data = orderRepository.findTopCoursesByRevenue(PageRequest.of(0, limit));
        List<TopPerformersResponse.TopRevenueCourse> list = data.stream().map(row -> {
            UUID courseId = (UUID) row[0];
            String title = (String) row[1];
            String slug = (String) row[2];
            String instructorName = (String) row[3];
            Long revenue = (Long) row[4];
            String currency = (String) row[5];
            Long orders = (Long) row[6];
            Long enrollmentCount = (Long) row[7];
            
            BigDecimal avgOrderValue = orders > 0
                    ? BigDecimal.valueOf(revenue).divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            
            return new TopPerformersResponse.TopRevenueCourse(
                    courseId, title, slug, instructorName, revenue, currency,
                    orders, enrollmentCount, avgOrderValue, 0
            );
        }).collect(Collectors.toList());
        
        for (int i = 0; i < list.size(); i++) {
            TopPerformersResponse.TopRevenueCourse old = list.get(i);
            list.set(i, new TopPerformersResponse.TopRevenueCourse(
                old.id(), old.title(), old.slug(), old.instructorName(), old.totalRevenueCents(),
                old.currency(), old.totalOrders(), old.enrollmentCount(), old.averageOrderValue(), i + 1
            ));
        }
        return new TopPerformersResponse(null, null, list);
    }

    @Override
    public byte[] exportDashboardData(String type) {
        // 1. Tạo Context cho Thymeleaf
        Context context = new Context();
        context.setVariable("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        context.setVariable("reportType", formatReportType(type));

        // 2. Chuẩn bị dữ liệu để render vào bảng
        prepareDataForPdf(type, context);

        // 3. Render HTML từ Thymeleaf Template (resources/templates/dashboard-report.html)
        String htmlContent = templateEngine.process("dashboard-report", context);


        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            try {
                // 1. Đăng ký Font Thường
                builder.useFont(() -> {
                    try {
                        return new ClassPathResource("fonts/DejaVuSans.ttf").getInputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, "DejaVu Sans"); // Family Name

                // 2. Đăng ký Font Đậm (Bold) cho thẻ <h1>, <th>
                builder.useFont(() -> {
                    try {
                        return new ClassPathResource("fonts/DejaVuSans-Bold.ttf").getInputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, "DejaVu Sans", 700, BaseRendererBuilder.FontStyle.NORMAL, true); 


            } catch (Exception e) {
                // Log lỗi 
                System.err.println("WARNING: Could not load custom fonts. Vietnamese text may fail.");
                e.printStackTrace();
            }

            builder.withHtmlContent(htmlContent, null);
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF export", e);
        }
    }

    private String formatReportType(String type) {
        if (type == null) return "Unknown Report";
        return type.replace("-", " ").toUpperCase();
    }

    /**
     * Hàm helper để map dữ liệu từ các hàm Service sang cấu trúc List để HTML dễ loop
     */
    private void prepareDataForPdf(String type, Context context) {
        List<String> headers = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();

        switch (type.toLowerCase()) {
            case "summary":
                // Với summary, ta không dùng bảng generic mà dùng layout riêng trong HTML
                context.setVariable("summary", getDashboardSummary());
                break;

            case "user-growth":
                context.setVariable("tableTitle", "User Growth Statistics");
                headers.addAll(List.of("Month", "New Users", "Active Users"));
                UserGrowthResponse userGrowth = getUserGrowthChart(24); // Lấy 24 tháng
                for (UserGrowthResponse.MonthlyData d : userGrowth.monthlyData()) {
                    rows.add(List.of(d.month(), d.newUsers(), d.activeUsers()));
                }
                break;

            case "revenue":
                context.setVariable("tableTitle", "Revenue Statistics");
                headers.addAll(List.of("Month", "VND", "USD", "Orders", "Avg Order Value"));
                RevenueChartResponse revenue = getRevenueChart(12);
                for (RevenueChartResponse.MonthlyRevenue r : revenue.monthlyRevenue()) {
                    rows.add(List.of(r.month(), r.revenueVND(), r.revenueUSD(), r.totalOrders(), r.averageOrderValue()));
                }
                break;
            
            case "enrollments":
                context.setVariable("tableTitle", "Enrollment Statistics");
                headers.addAll(List.of("Month", "New Enroll", "Completed", "Rate (%)", "Avg Progress (%)"));
                EnrollmentChartResponse enrollment = getEnrollmentChart(12);
                for (EnrollmentChartResponse.MonthlyEnrollment e : enrollment.monthlyEnrollment()) {
                    rows.add(List.of(e.month(), e.newEnrollments(), e.completedEnrollments(), e.completionRate(), e.averageProgress()));
                }
                break;

            case "top-courses":
                context.setVariable("tableTitle", "Top 50 Performing Courses");
                headers.addAll(List.of("Rank", "Title", "Instructor", "Enrollments", "Revenue (VND)"));
                // Lấy 50 khóa học -> Hiển thị hết trong PDF (nhiều trang)
                TopPerformersResponse courses = getTopCourses(50);
                for (TopPerformersResponse.TopCourse c : courses.topCourses()) {
                    rows.add(List.of(c.rank(), c.title(), c.instructorName(), c.enrollmentCount(), c.totalRevenueCents()));
                }
                break;
            
            case "top-instructors":
                context.setVariable("tableTitle", "Top 50 Instructors");
                headers.addAll(List.of("Rank", "Name", "Email", "Courses", "Enrollments", "Revenue"));
                TopPerformersResponse instructors = getTopInstructors(50);
                for (TopPerformersResponse.TopInstructor i : instructors.topInstructors()) {
                    rows.add(List.of(i.rank(), i.fullName(), i.email(), i.totalCourses(), i.totalEnrollments(), i.totalRevenueCents()));
                }
                break;

            case "top-revenue-courses":
                context.setVariable("tableTitle", "Top 50 Highest Revenue Courses");
                headers.addAll(List.of("Rank", "Title", "Instructor", "Orders", "Revenue", "AOV"));
                TopPerformersResponse revCourses = getTopRevenueCourses(50);
                for (TopPerformersResponse.TopRevenueCourse c : revCourses.topRevenueCourses()) {
                    rows.add(List.of(c.rank(), c.title(), c.instructorName(), c.totalOrders(), c.totalRevenueCents(), c.averageOrderValue()));
                }
                break;

            default:
                // Mặc định trả về summary
                context.setVariable("summary", getDashboardSummary());
                break;
        }

        // Đẩy dữ liệu bảng vào context nếu có
        if (!headers.isEmpty()) {
            context.setVariable("headers", headers);
            context.setVariable("tableData", rows);
        }
    }
}