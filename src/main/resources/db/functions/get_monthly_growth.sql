-- Function to get monthly growth statistics for an instructor
-- Returns revenue and student count for each weekly period within a month
-- Periods: 1-7, 8-14, 15-21, 22-28, 29-end

CREATE OR REPLACE FUNCTION get_monthly_growth(
    instructor_id UUID,
    target_year INTEGER,
    target_month INTEGER
)
RETURNS TABLE (
    period_start INTEGER,
    period_end INTEGER,
    revenue_cents BIGINT,
    student_count BIGINT
) AS $$
DECLARE
    last_day INTEGER;
BEGIN
    -- Get the last day of the month
    last_day := EXTRACT(DAY FROM (DATE_TRUNC('MONTH', MAKE_DATE(target_year, target_month, 1)) + INTERVAL '1 MONTH - 1 DAY'))::INTEGER;
    
    -- Return period statistics
    RETURN QUERY
    WITH periods AS (
        -- Define the 5 periods
        SELECT 1 AS period_start, 7 AS period_end
        UNION ALL SELECT 8, 14
        UNION ALL SELECT 15, 21
        UNION ALL SELECT 22, 28
        UNION ALL SELECT 29, last_day WHERE last_day > 28
    ),
    period_dates AS (
        SELECT 
            p.period_start,
            p.period_end,
            MAKE_TIMESTAMP(target_year, target_month, p.period_start, 0, 0, 0) AT TIME ZONE 'UTC' AS start_time,
            MAKE_TIMESTAMP(target_year, target_month, p.period_end, 23, 59, 59.999) AT TIME ZONE 'UTC' AS end_time
        FROM periods p
    ),
    revenue_data AS (
        SELECT 
            pd.period_start,
            pd.period_end,
            COALESCE(SUM(oi.total_price_cents), 0)::BIGINT AS revenue_cents
        FROM period_dates pd
        LEFT JOIN orders o ON 
            o.paid_at >= pd.start_time 
            AND o.paid_at <= pd.end_time
            AND o.status = 'PAID'
        LEFT JOIN order_items oi ON 
            oi.order_id = o.id 
            AND oi.entity = 'COURSE'
        LEFT JOIN courses c ON 
            c.id = oi.entity_id 
            AND c.created_by = instructor_id
        GROUP BY pd.period_start, pd.period_end
    ),
    student_data AS (
        SELECT 
            pd.period_start,
            pd.period_end,
            COUNT(DISTINCT e.user_id)::BIGINT AS student_count
        FROM period_dates pd
        LEFT JOIN enrollments e ON 
            e.created_at >= pd.start_time 
            AND e.created_at <= pd.end_time
            AND e.status = 'ACTIVE'
        LEFT JOIN courses c ON 
            c.id = e.course_id 
            AND c.created_by = instructor_id
        GROUP BY pd.period_start, pd.period_end
    )
    SELECT 
        r.period_start,
        r.period_end,
        r.revenue_cents,
        COALESCE(s.student_count, 0)::BIGINT AS student_count
    FROM revenue_data r
    LEFT JOIN student_data s ON 
        s.period_start = r.period_start 
        AND s.period_end = r.period_end
    ORDER BY r.period_start;
END;
$$ LANGUAGE plpgsql;
