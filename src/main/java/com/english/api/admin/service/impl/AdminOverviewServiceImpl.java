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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import com.english.api.admin.repository.DashboardStatsRepository;
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

        // DÙNG REPOSITORY MỚI
        Map<String, Long> stats = dashboardStatsRepository.getAllDashboardStats(
            weekAgo, sevenDaysAgo, threeDaysAgo,
            startOfMonth, endOfMonth, startPrevMonth, endPrevMonth
        );

        long revenueThisMonth = get(stats, "REVENUE_THIS_MONTH");
        long ordersPaid = get(stats, "ORDERS_PAID");
        BigDecimal aov = ordersPaid > 0
            ? BigDecimal.valueOf(revenueThisMonth).divide(BigDecimal.valueOf(ordersPaid), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        long totalVndUsd = get(stats, "REVENUE_VND_TOTAL") + get(stats, "REVENUE_USD_TOTAL");

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
                .totalCentsUSD(get(stats, "REVENUE_USD_TOTAL"))
                .vndPercentage(pct(get(stats, "REVENUE_VND_TOTAL"), totalVndUsd))
                .usdPercentage(pct(get(stats, "REVENUE_USD_TOTAL"), totalVndUsd))
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

    // Helper methods
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

    // === 2. PENDING ACTIONS ===
    @Override
    public PendingActionsResponse getPendingActions() {
        Long totalPendingRequest = instructorRequestRepository.countByStatus(InstructorRequest.Status.PENDING);
        Long totalUnsolveReport = forumReportRepository.countByResolvedAtIsNull();
        Long totalPendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);


        return PendingActionsResponse.builder()
                .instructorRequestsCount((long) totalPendingRequest)
                .forumReportsCount((long) totalUnsolveReport)
                .pendingOrdersCount((long) totalPendingOrders)
                .build();
    }

    // === 3. CHARTS ===
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
            monthMap.put(key, UserGrowthResponse.MonthlyData.builder().month(key).newUsers(0L).activeUsers(0L).build());
            current = current.plusMonths(1);
        }

        for (Object[] row : data) {
            String monthStr = ((java.sql.Date) row[0]).toLocalDate().withDayOfMonth(1)
                    .format(DateTimeFormatter.ofPattern("MM-yyyy"));
            Long newUsers = (Long) row[1];
            Long activeUsers = (Long) row[2];
            UserGrowthResponse.MonthlyData d = monthMap.get(monthStr);
            if (d != null) {
                d.setNewUsers(newUsers);
                d.setActiveUsers(activeUsers);
            }
        }

        return UserGrowthResponse.builder()
                .monthlyData(new ArrayList<>(monthMap.values()))
                .build();
    }

    @Override
    public RevenueChartResponse getRevenueChart(int months) {
        if (months > 12) months = 12;
        OffsetDateTime startDate = OffsetDateTime.now().minusMonths(months).withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);
        // List<Object[]> data = orderRepository.getRevenueByMonth(startDate);
        List<Object[]> data = orderRepository.getRevenueByMonth(startDate);

        Map<String, RevenueChartResponse.MonthlyRevenue> monthMap = new LinkedHashMap<>();
        LocalDate current = LocalDate.now().withDayOfMonth(1).minusMonths(months - 1);
        LocalDate end = LocalDate.now().withDayOfMonth(1);
        while (!current.isAfter(end)) {
            String key = current.format(DateTimeFormatter.ofPattern("MM-yyyy"));
            monthMap.put(key, RevenueChartResponse.MonthlyRevenue.builder()
                    .month(key).revenueVND(0L).revenueUSD(0L).totalOrders(0L).averageOrderValue(BigDecimal.ZERO)
                    .build());
            current = current.plusMonths(1);
        }

        for (Object[] row : data) {
            String monthStr = ((LocalDateTime) row[0]).toLocalDate().withDayOfMonth(1)
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

            RevenueChartResponse.MonthlyRevenue d = monthMap.get(monthStr);
            if (d != null) {
                d.setRevenueVND(vnd);
                d.setRevenueUSD(usd);
                d.setTotalOrders(orders);
                d.setAverageOrderValue(avg);
            }
        }

        return RevenueChartResponse.builder()
                .monthlyRevenue(new ArrayList<>(monthMap.values()))
                .build();
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
            monthMap.put(key, EnrollmentChartResponse.MonthlyEnrollment.builder()
                    .month(key).newEnrollments(0L).completedEnrollments(0L)
                    .completionRate(BigDecimal.ZERO).averageProgress(BigDecimal.ZERO)
                    .build());
            current = current.plusMonths(1);
        }

        for (Object[] row : data) {
            String monthStr = ((LocalDateTime) row[0]).toLocalDate().withDayOfMonth(1)
                    .format(DateTimeFormatter.ofPattern("MM-yyyy"));
            Long newEnroll = (Long) row[1];
            Long completed = (Long) row[2];
            Double avgProgress = (Double) row[3];
            BigDecimal completionRate = newEnroll > 0
                    ? BigDecimal.valueOf(completed * 100.0 / newEnroll).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            EnrollmentChartResponse.MonthlyEnrollment d = monthMap.get(monthStr);
            if (d != null) {
                d.setNewEnrollments(newEnroll);
                d.setCompletedEnrollments(completed);
                d.setCompletionRate(completionRate);
                d.setAverageProgress(BigDecimal.valueOf(avgProgress != null ? avgProgress : 0).setScale(2, RoundingMode.HALF_UP));
            }
        }

        return EnrollmentChartResponse.builder()
                .monthlyEnrollment(new ArrayList<>(monthMap.values()))
                .build();
    }

    // === 4. TOP PERFORMERS ===
    @Override
    public TopPerformersResponse getTopCourses(int limit) {
        if (limit > 50) limit = 50;
        List<Object[]> data = enrollmentRepository.findTopCoursesByEnrollmentCount(PageRequest.of(0, limit));
        List<TopPerformersResponse.TopCourse> list = data.stream().map(row -> {
            UUID courseId = (UUID) row[0];
            Long count = (Long) row[1];
            return TopPerformersResponse.TopCourse.builder()
                    .id(courseId).enrollmentCount(count).rank(0).build();
        }).collect(Collectors.toList());
        TopPerformersResponse response = new TopPerformersResponse();
        response.setTopCourses(list);
        return response;
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

                return TopPerformersResponse.TopInstructor.builder()
                        .id(instructorId)
                        .fullName(fullName)
                        .email(email)
                        .avatarUrl(avatarUrl)
                        .totalRevenueCents(totalRevenueCents)
                        .totalCourses(totalCourses)
                        .totalEnrollments(totalEnrollments)
                        .rank(0)
                        .build();
                })
                .collect(Collectors.toList());

        // Gán rank
        for (int i = 0; i < list.size(); i++) {
                list.get(i).setRank(i + 1);
        }

        TopPerformersResponse response = new TopPerformersResponse();
        response.setTopInstructors(list);
        return response;
        }



    @Override
    public TopPerformersResponse getTopRevenueCourses(int limit) {
        if (limit > 50) limit = 50;
        List<Object[]> data = orderRepository.findTopCoursesByRevenue(PageRequest.of(0, limit));
        List<TopPerformersResponse.TopRevenueCourse> list = data.stream().map(row -> {
            UUID courseId = (UUID) row[0];
            Long revenue = (Long) row[1];
            Long orders = (Long) row[2];
            return TopPerformersResponse.TopRevenueCourse.builder()
                    .id(courseId).totalRevenueCents(revenue).totalOrders(orders).rank(0).build();
        }).collect(Collectors.toList());
        TopPerformersResponse response = new TopPerformersResponse();
        response.setTopRevenueCourses(list);
        return response;
    }
    
}