CREATE OR REPLACE FUNCTION get_all_dashboard_stats(
    week_ago TIMESTAMPTZ,
    seven_days_ago TIMESTAMPTZ,
    three_days_ago TIMESTAMPTZ,
    start_of_month TIMESTAMPTZ,
    end_of_month TIMESTAMPTZ,
    start_prev_month TIMESTAMPTZ,
    end_prev_month TIMESTAMPTZ
)
RETURNS TABLE (
    metric TEXT,
    value BIGINT
)
LANGUAGE sql
STABLE
AS $$
    WITH 
    -- USERS
    users_data AS (
        SELECT 
            COUNT(*) AS total,
            COUNT(*) FILTER (WHERE is_active = TRUE) AS active,
            COUNT(*) FILTER (WHERE email_verified = TRUE) AS verified,
            COUNT(*) FILTER (WHERE created_at > week_ago) AS week_growth
        FROM users
    ),

    -- INSTRUCTORS
    instructors_data AS (
        SELECT COUNT(*) AS total FROM instructors
    ),

    instructor_requests_data AS (
        SELECT 
            COUNT(*) FILTER (WHERE status = 'PENDING') AS pending,
            COUNT(*) FILTER (WHERE status = 'PENDING' AND requested_at < seven_days_ago) AS over_7d,
            COUNT(*) FILTER (WHERE status = 'PENDING' AND requested_at BETWEEN three_days_ago AND seven_days_ago) AS between_3_7d,
            COUNT(*) FILTER (WHERE status = 'PENDING' AND requested_at > three_days_ago) AS under_3d
        FROM instructor_requests
    ),

    -- COURSES
    courses_data AS (
        SELECT 
            COUNT(*) FILTER (WHERE is_deleted = FALSE) AS total,
            COUNT(*) FILTER (WHERE is_deleted = FALSE AND status = 'PUBLISHED') AS published,
            COUNT(*) FILTER (WHERE is_deleted = FALSE AND status = 'DRAFT') AS draft,
            COUNT(*) FILTER (WHERE is_deleted = FALSE AND status = 'REJECTED') AS rejected
        FROM courses
    ),

    course_modules_data AS (
        SELECT COUNT(*) AS total FROM course_modules
    ),

    lessons_data AS (
        SELECT 
            COUNT(*) AS total,
            COUNT(*) FILTER (WHERE is_free = TRUE) AS free
        FROM lessons
    ),

    -- QUIZZES
    quizzes_data AS (
        SELECT 
            COUNT(*) AS total,
            COUNT(*) FILTER (WHERE status = 'PUBLISHED') AS published,
            COUNT(*) FILTER (WHERE status = 'DRAFT') AS draft
        FROM quizzes
    ),

    questions_data AS (
        SELECT COUNT(*) AS total FROM questions
    ),

    quiz_skills_data AS (
        SELECT 
            COUNT(*) FILTER (WHERE qs.skill = 'READING') AS reading,
            COUNT(*) FILTER (WHERE qs.skill = 'LISTENING') AS listening,
            COUNT(*) FILTER (WHERE qs.skill = 'WRITING') AS writing,
            COUNT(*) FILTER (WHERE qs.skill = 'SPEAKING') AS speaking
        FROM quizzes q
        JOIN quiz_sections qs ON qs.id = q.quiz_section_id
    ),

    -- ORDERS / REVENUE
    orders_data AS (
        SELECT 
            COALESCE(SUM(total_cents) FILTER (WHERE status = 'PAID' AND created_at BETWEEN start_of_month AND end_of_month), 0) AS revenue_this_month,
            COALESCE(SUM(total_cents) FILTER (WHERE status = 'PAID' AND created_at BETWEEN start_prev_month AND end_prev_month), 0) AS revenue_prev_month,
            COALESCE(SUM(total_cents) FILTER (WHERE currency = 'VND' AND status = 'PAID'), 0) AS revenue_vnd,
            COALESCE(SUM(total_cents) FILTER (WHERE currency = 'USD' AND status = 'PAID'), 0) AS revenue_usd,
            COUNT(*) FILTER (WHERE created_at > start_of_month) AS this_month,
            COUNT(*) FILTER (WHERE status = 'PAID') AS paid,
            COUNT(*) FILTER (WHERE status = 'PENDING') AS pending,
            COUNT(*) FILTER (WHERE status = 'CANCELLED') AS cancelled
        FROM orders
    ),

    -- CART
    cart_items_data AS (
        SELECT COUNT(*) AS total FROM cart_items
    ),

    -- PAYMENTS
    payments_data AS (
        SELECT 
            COUNT(*) AS total,
            COUNT(*) FILTER (WHERE provider = 'PAYPAL') AS paypal,
            COUNT(*) FILTER (WHERE provider = 'PAYOS') AS payos,
            COUNT(*) FILTER (WHERE status = 'SUCCESS') AS success,
            COUNT(*) FILTER (WHERE status = 'FAILED') AS failed,
            COUNT(*) FILTER (WHERE status = 'REFUNDED') AS refunded
        FROM payments
    ),

    -- ENROLLMENTS / PROGRESS
    enrollments_data AS (
        SELECT 
            COUNT(*) AS total,
            COUNT(*) FILTER (WHERE status = 'COMPLETED') AS completed,
            COUNT(*) FILTER (WHERE status = 'CANCELLED') AS cancelled,
            COALESCE(AVG(progress_percent) FILTER (WHERE status = 'ACTIVE'), 0) AS avg_progress
        FROM enrollments
    ),

    quiz_attempts_data AS (
        SELECT COUNT(*) AS total FROM quiz_attempts
    ),

    -- BLOG
    blog_data AS (
        SELECT 
            COUNT(*) AS total,
            COUNT(*) FILTER (WHERE is_published = TRUE) AS published,
            COUNT(*) FILTER (WHERE is_published = FALSE) AS draft
        FROM blog_posts
    ),

    blog_comments_data AS (
        SELECT COUNT(*) AS total FROM blog_comments
    ),

    -- FORUM
    forum_threads_data AS (
        SELECT 
            COUNT(*) AS total_threads,
            COUNT(*) FILTER (WHERE is_locked = TRUE) AS locked_threads,
            COALESCE(SUM(view_count), 0) AS total_views
        FROM forum_threads
    ),

    forum_posts_data AS (
        SELECT COUNT(*) AS total FROM forum_posts
    )


    -- FINAL RESULTS (GIỮ NGUYÊN Y CHUỖI METRIC)
    SELECT * FROM (
        VALUES
            -- Users
            ('USERS_TOTAL', (SELECT total FROM users_data)),
            ('USERS_ACTIVE', (SELECT active FROM users_data)),
            ('USERS_VERIFIED', (SELECT verified FROM users_data)),
            ('USERS_WEEK_GROWTH', (SELECT week_growth FROM users_data)),

            -- Instructors
            ('INSTRUCTORS_TOTAL', (SELECT total FROM instructors_data)),
            ('INSTRUCTOR_REQUESTS_PENDING', (SELECT pending FROM instructor_requests_data)),
            ('INSTRUCTOR_REQUESTS_OVER_7D', (SELECT over_7d FROM instructor_requests_data)),
            ('INSTRUCTOR_REQUESTS_3_7D', (SELECT between_3_7d FROM instructor_requests_data)),
            ('INSTRUCTOR_REQUESTS_UNDER_3D', (SELECT under_3d FROM instructor_requests_data)),

            -- Courses
            ('COURSES_TOTAL', (SELECT total FROM courses_data)),
            ('COURSES_PUBLISHED', (SELECT published FROM courses_data)),
            ('COURSES_DRAFT', (SELECT draft FROM courses_data)),
            ('COURSES_REJECTED', (SELECT rejected FROM courses_data)),
            ('COURSE_MODULES_TOTAL', (SELECT total FROM course_modules_data)),
            ('LESSONS_TOTAL', (SELECT total FROM lessons_data)),
            ('LESSONS_FREE', (SELECT free FROM lessons_data)),

            -- Quizzes
            ('QUIZZES_TOTAL', (SELECT total FROM quizzes_data)),
            ('QUESTIONS_TOTAL', (SELECT total FROM questions_data)),
            ('QUIZZES_READING', (SELECT reading FROM quiz_skills_data)),
            ('QUIZZES_LISTENING', (SELECT listening FROM quiz_skills_data)),
            ('QUIZZES_WRITING', (SELECT writing FROM quiz_skills_data)),
            ('QUIZZES_SPEAKING', (SELECT speaking FROM quiz_skills_data)),
            ('QUIZZES_PUBLISHED', (SELECT published FROM quizzes_data)),
            ('QUIZZES_DRAFT', (SELECT draft FROM quizzes_data)),

            -- Revenue & Orders
            ('REVENUE_THIS_MONTH', (SELECT revenue_this_month FROM orders_data)),
            ('REVENUE_PREV_MONTH', (SELECT revenue_prev_month FROM orders_data)),
            ('REVENUE_VND_TOTAL', (SELECT revenue_vnd FROM orders_data)),
            ('REVENUE_USD_TOTAL', (SELECT revenue_usd FROM orders_data)),
            ('ORDERS_THIS_MONTH', (SELECT this_month FROM orders_data)),
            ('ORDERS_PAID', (SELECT paid FROM orders_data)),
            ('ORDERS_PENDING', (SELECT pending FROM orders_data)),
            ('ORDERS_CANCELLED', (SELECT cancelled FROM orders_data)),
            ('CART_ITEMS_UNPAID', (SELECT total FROM cart_items_data)),

            -- Payments
            ('PAYMENTS_TOTAL', (SELECT total FROM payments_data)),
            ('PAYMENTS_PAYPAL', (SELECT paypal FROM payments_data)),
            ('PAYMENTS_PAYOS', (SELECT payos FROM payments_data)),
            ('PAYMENTS_SUCCESS', (SELECT success FROM payments_data)),
            ('PAYMENTS_FAILED', (SELECT failed FROM payments_data)),
            ('PAYMENTS_REFUNDED', (SELECT refunded FROM payments_data)),

            -- Enrollments
            ('ENROLLMENTS_TOTAL', (SELECT total FROM enrollments_data)),
            ('ENROLLMENTS_COMPLETED', (SELECT completed FROM enrollments_data)),
            ('ENROLLMENTS_CANCELLED', (SELECT cancelled FROM enrollments_data)),
            ('ENROLLMENTS_AVG_PROGRESS', (SELECT avg_progress FROM enrollments_data)),
            ('QUIZ_ATTEMPTS_TOTAL', (SELECT total FROM quiz_attempts_data)),

            -- Blog
            ('BLOG_POSTS_TOTAL', (SELECT total FROM blog_data)),
            ('BLOG_POSTS_PUBLISHED', (SELECT published FROM blog_data)),
            ('BLOG_POSTS_DRAFT', (SELECT draft FROM blog_data)),
            ('BLOG_COMMENTS_TOTAL', (SELECT total FROM blog_comments_data)),

            -- Forum
            ('FORUM_THREADS_TOTAL', (SELECT total_threads FROM forum_threads_data)),
            ('FORUM_POSTS_TOTAL', (SELECT total FROM forum_posts_data)),
            ('FORUM_VIEWS_TOTAL', (SELECT total_views FROM forum_threads_data)),
            ('FORUM_THREADS_LOCKED', (SELECT locked_threads FROM forum_threads_data))
    ) AS t(metric, value);
$$;
