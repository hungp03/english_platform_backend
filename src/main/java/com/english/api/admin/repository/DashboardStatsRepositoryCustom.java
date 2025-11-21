package com.english.api.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DashboardStatsRepositoryCustom {

    private final EntityManager em;

    public Map<String, Long> getAllDashboardStats(
            OffsetDateTime weekAgo,
            Instant sevenDaysAgo,
            Instant threeDaysAgo,
            OffsetDateTime startOfMonth,
            OffsetDateTime endOfMonth,
            OffsetDateTime startPrevMonth,
            OffsetDateTime endPrevMonth
    ) {
        String sql = """
            WITH stats AS (
                -- Users
                SELECT 'USERS_TOTAL' as metric, COUNT(*) as value FROM users
                UNION ALL SELECT 'USERS_ACTIVE', COUNT(*) FROM users WHERE is_active = true
                UNION ALL SELECT 'USERS_VERIFIED', COUNT(*) FROM users WHERE email_verified = true
                UNION ALL SELECT 'USERS_WEEK_GROWTH', COUNT(*) FROM users WHERE created_at > :weekAgo
               
                -- Instructors
                UNION ALL SELECT 'INSTRUCTORS_TOTAL', COUNT(*) FROM instructors
                UNION ALL SELECT 'INSTRUCTOR_REQUESTS_PENDING', COUNT(*) FROM instructor_requests WHERE status = 'PENDING'
                UNION ALL SELECT 'INSTRUCTOR_REQUESTS_OVER_7D', COUNT(*) FROM instructor_requests WHERE status = 'PENDING' AND requested_at < :sevenDaysAgo
                UNION ALL SELECT 'INSTRUCTOR_REQUESTS_3_7D', COUNT(*) FROM instructor_requests WHERE status = 'PENDING' AND requested_at BETWEEN :threeDaysAgo AND :sevenDaysAgo
                UNION ALL SELECT 'INSTRUCTOR_REQUESTS_UNDER_3D', COUNT(*) FROM instructor_requests WHERE status = 'PENDING' AND requested_at > :threeDaysAgo
               
                -- Courses
                UNION ALL SELECT 'COURSES_TOTAL', COUNT(*) FROM courses WHERE is_deleted = false
                UNION ALL SELECT 'COURSES_PUBLISHED', COUNT(*) FROM courses WHERE is_deleted = false AND status = 'PUBLISHED'
                UNION ALL SELECT 'COURSES_DRAFT', COUNT(*) FROM courses WHERE is_deleted = false AND status = 'DRAFT'
                UNION ALL SELECT 'COURSES_REJECTED', COUNT(*) FROM courses WHERE is_deleted = false AND status = 'REJECTED'
                UNION ALL SELECT 'COURSE_MODULES_TOTAL', COUNT(*) FROM course_modules
                UNION ALL SELECT 'LESSONS_TOTAL', COUNT(*) FROM lessons
                UNION ALL SELECT 'LESSONS_FREE', COUNT(*) FROM lessons WHERE is_free = true
               
                -- Quizzes
                UNION ALL SELECT 'QUIZZES_TOTAL', COUNT(*) FROM quizzes
                UNION ALL SELECT 'QUESTIONS_TOTAL', COUNT(*) FROM questions
                UNION ALL SELECT 'QUIZZES_READING', COUNT(*) FROM quizzes q JOIN quiz_sections qs ON qs.id = q.quiz_section_id WHERE qs.skill = 'READING'
                UNION ALL SELECT 'QUIZZES_LISTENING', COUNT(*) FROM quizzes q JOIN quiz_sections qs ON qs.id = q.quiz_section_id WHERE qs.skill = 'LISTENING'
                UNION ALL SELECT 'QUIZZES_WRITING', COUNT(*) FROM quizzes q JOIN quiz_sections qs ON qs.id = q.quiz_section_id WHERE qs.skill = 'WRITING'
                UNION ALL SELECT 'QUIZZES_SPEAKING', COUNT(*) FROM quizzes q JOIN quiz_sections qs ON qs.id = q.quiz_section_id WHERE qs.skill = 'SPEAKING'
                UNION ALL SELECT 'QUIZZES_PUBLISHED', COUNT(*) FROM quizzes WHERE status = 'PUBLISHED'
                UNION ALL SELECT 'QUIZZES_DRAFT', COUNT(*) FROM quizzes WHERE status = 'DRAFT'
               
                -- Revenue
                UNION ALL SELECT 'REVENUE_THIS_MONTH', COALESCE(SUM(total_cents), 0) FROM orders WHERE status = 'PAID' AND created_at BETWEEN :startOfMonth AND :endOfMonth
                UNION ALL SELECT 'REVENUE_PREV_MONTH', COALESCE(SUM(total_cents), 0) FROM orders WHERE status = 'PAID' AND created_at BETWEEN :startPrevMonth AND :endPrevMonth
                UNION ALL SELECT 'REVENUE_VND_TOTAL', COALESCE(SUM(total_cents), 0) FROM orders WHERE currency = 'VND' AND status = 'PAID'
                UNION ALL SELECT 'REVENUE_USD_TOTAL', COALESCE(SUM(total_cents), 0) FROM orders WHERE currency = 'USD' AND status = 'PAID'
               
                -- Orders
                UNION ALL SELECT 'ORDERS_THIS_MONTH', COUNT(*) FROM orders WHERE created_at > :startOfMonth
                UNION ALL SELECT 'ORDERS_PAID', COUNT(*) FROM orders WHERE status = 'PAID'
                UNION ALL SELECT 'ORDERS_PENDING', COUNT(*) FROM orders WHERE status = 'PENDING'
                UNION ALL SELECT 'ORDERS_CANCELLED', COUNT(*) FROM orders WHERE status = 'CANCELLED'
                UNION ALL SELECT 'CART_ITEMS_UNPAID', COUNT(*) FROM cart_items
               
                -- Payments
                UNION ALL SELECT 'PAYMENTS_TOTAL', COUNT(*) FROM payments
                UNION ALL SELECT 'PAYMENTS_PAYPAL', COUNT(*) FROM payments WHERE provider = 'PAYPAL'
                UNION ALL SELECT 'PAYMENTS_PAYOS', COUNT(*) FROM payments WHERE provider = 'PAYOS'
                UNION ALL SELECT 'PAYMENTS_SUCCESS', COUNT(*) FROM payments WHERE status = 'SUCCESS'
                UNION ALL SELECT 'PAYMENTS_FAILED', COUNT(*) FROM payments WHERE status = 'FAILED'
                UNION ALL SELECT 'PAYMENTS_REFUNDED', COUNT(*) FROM payments WHERE status = 'REFUNDED'
               
                -- Learning
                UNION ALL SELECT 'ENROLLMENTS_TOTAL', COUNT(*) FROM enrollments
                UNION ALL SELECT 'ENROLLMENTS_COMPLETED', COUNT(*) FROM enrollments WHERE status = 'COMPLETED'
                UNION ALL SELECT 'ENROLLMENTS_CANCELLED', COUNT(*) FROM enrollments WHERE status = 'CANCELLED'
                UNION ALL SELECT 'ENROLLMENTS_AVG_PROGRESS', COALESCE(AVG(progress_percent), 0) FROM enrollments WHERE status = 'ACTIVE'
                UNION ALL SELECT 'QUIZ_ATTEMPTS_TOTAL', COUNT(*) FROM quiz_attempts
               
                -- Content
                UNION ALL SELECT 'BLOG_POSTS_TOTAL', COUNT(*) FROM blog_posts
                UNION ALL SELECT 'BLOG_POSTS_PUBLISHED', COUNT(*) FROM blog_posts WHERE is_published = true
                UNION ALL SELECT 'BLOG_POSTS_DRAFT', COUNT(*) FROM blog_posts WHERE is_published = false
                UNION ALL SELECT 'BLOG_COMMENTS_TOTAL', COUNT(*) FROM blog_comments
                UNION ALL SELECT 'FORUM_THREADS_TOTAL', COUNT(*) FROM forum_threads
                UNION ALL SELECT 'FORUM_POSTS_TOTAL', COUNT(*) FROM forum_posts
                UNION ALL SELECT 'FORUM_VIEWS_TOTAL', COALESCE(SUM(view_count), 0) FROM forum_threads
                UNION ALL SELECT 'FORUM_THREADS_LOCKED', COUNT(*) FROM forum_threads WHERE is_locked = true
            )
            SELECT metric, value FROM stats
            """;

        Query query = em.createNativeQuery(sql);
        query.setParameter("weekAgo", weekAgo);
        query.setParameter("sevenDaysAgo", sevenDaysAgo);
        query.setParameter("threeDaysAgo", threeDaysAgo);
        query.setParameter("startOfMonth", startOfMonth);
        query.setParameter("endOfMonth", endOfMonth);
        query.setParameter("startPrevMonth", startPrevMonth);
        query.setParameter("endPrevMonth", endPrevMonth);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue(),
                        (v1, v2) -> v1
                ));
    }
}