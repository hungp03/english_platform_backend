-- Function to get instructor statistics
-- Returns: total courses, published courses, total students, and total revenue

CREATE OR REPLACE FUNCTION get_instructor_stats(instructor_id UUID)
RETURNS TABLE (
    total_courses BIGINT,
    published_courses BIGINT,
    total_students BIGINT,
    total_revenue_cents BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        -- Total number of courses created by instructor
        COUNT(DISTINCT c.id)::BIGINT AS total_courses,
        
        -- Number of published courses
        COUNT(DISTINCT CASE WHEN c.status = 'PUBLISHED' THEN c.id END)::BIGINT AS published_courses,
        
        -- Total number of unique students enrolled in instructor's courses
        COUNT(DISTINCT e.user_id)::BIGINT AS total_students,
        
        -- Total revenue from successful course purchases (PAID orders only, after discount)
        COALESCE(SUM(
            CASE 
                WHEN o.status = 'PAID' AND oi.entity = 'COURSE' 
                THEN oi.unit_price_cents * oi.quantity - COALESCE(oi.discount_cents, 0)
                ELSE 0 
            END
        ), 0)::BIGINT AS total_revenue_cents
        
    FROM courses c
    LEFT JOIN enrollments e ON e.course_id = c.id AND e.status = 'ACTIVE'
    LEFT JOIN order_items oi ON oi.entity_id = c.id AND oi.entity = 'COURSE'
    LEFT JOIN orders o ON o.id = oi.order_id
    WHERE c.created_by = instructor_id
      AND c.is_deleted = false;
END;
$$ LANGUAGE plpgsql;

-- Create index to optimize the function if not exists
CREATE INDEX IF NOT EXISTS idx_order_items_entity_and_id ON order_items(entity, entity_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
