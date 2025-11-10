package com.english.api.course.repository.custom;

import com.english.api.course.dto.projection.CourseWithStatsProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.NativeQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of custom repository methods
 */
@Repository
@RequiredArgsConstructor
public class CourseRepositoryCustomImpl implements CourseRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    // Mapping between entity field names and database column names
    private static final Map<String, String> SORT_COLUMN_MAP = new HashMap<>();

    static {
        SORT_COLUMN_MAP.put("id", "c.id");
        SORT_COLUMN_MAP.put("title", "c.title");
        SORT_COLUMN_MAP.put("description", "c.description");
        SORT_COLUMN_MAP.put("language", "c.language");
        SORT_COLUMN_MAP.put("priceCents", "c.price_cents");
        SORT_COLUMN_MAP.put("createdAt", "c.created_at");
        SORT_COLUMN_MAP.put("updatedAt", "c.updated_at");
        SORT_COLUMN_MAP.put("status", "c.status");
        SORT_COLUMN_MAP.put("moduleCount", "module_count");
        SORT_COLUMN_MAP.put("lessonCount", "lesson_count");
    }

    @Override
    public Page<CourseWithStatsProjection> searchWithStats(
            String keyword,
            String status,
            String[] skills,
            Pageable pageable
    ) {
        String baseQuery = """
            SELECT c.id AS id,
                c.title AS title,
                c.slug AS slug,
                c.description AS description,
                c.language AS language,
                c.thumbnail AS thumbnail,
                c.skill_focus AS skill_focus,
                c.price_cents AS price_cents,
                c.currency AS currency,
                c.status AS status,
                COUNT(DISTINCT m.id) AS module_count,
                COUNT(DISTINCT l.id) AS lesson_count,
                c.created_at AS created_at,
                c.updated_at AS updated_at
            FROM courses c
            LEFT JOIN course_modules m ON m.course_id = c.id
            LEFT JOIN lessons l ON l.module_id = m.id
            WHERE (CAST(:keyword AS text) IS NULL OR
                   LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%'))
                   OR LOWER(c.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%')))
              AND (CAST(:status AS text) IS NULL OR c.status = CAST(:status AS text))
              AND (CAST(:status AS text) IS NOT NULL OR c.status != 'DRAFT')
              AND (:skillsCount = 0 OR
                   EXISTS (
                       SELECT 1 FROM unnest(c.skill_focus) AS skill
                       WHERE LOWER(skill) = ANY(
                           SELECT LOWER(unnest) FROM unnest(CAST(:skills AS text[]))
                       )
                   ))
            GROUP BY c.id, c.title, c.slug, c.description, c.language, c.thumbnail, c.skill_focus,
                     c.price_cents, c.currency, c.status, c.created_at, c.updated_at
            """;

        String countQuery = """
            SELECT COUNT(DISTINCT c.id)
            FROM courses c
            WHERE (CAST(:keyword AS text) IS NULL OR
                   LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%'))
                   OR LOWER(c.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%')))
              AND (CAST(:status AS text) IS NULL OR c.status = CAST(:status AS text))
              AND (CAST(:status AS text) IS NOT NULL OR c.status != 'DRAFT')
              AND (:skillsCount = 0 OR
                   EXISTS (
                       SELECT 1 FROM unnest(c.skill_focus) AS skill
                       WHERE LOWER(skill) = ANY(
                           SELECT LOWER(unnest) FROM unnest(CAST(:skills AS text[]))
                       )
                   ))
            """;

        return executeQuery(baseQuery, countQuery, keyword, status, null, skills, pageable);
    }

    @Override
    public Page<CourseWithStatsProjection> searchByOwnerWithStats(
            UUID ownerId,
            String keyword,
            String status,
            String[] skills,
            Pageable pageable
    ) {
        String baseQuery = """
            SELECT c.id AS id,
                c.title AS title,
                c.slug AS slug,
                c.description AS description,
                c.language AS language,
                c.thumbnail AS thumbnail,
                c.skill_focus AS skill_focus,
                c.price_cents AS price_cents,
                c.currency AS currency,
                c.status AS status,
                COUNT(DISTINCT m.id) AS module_count,
                COUNT(DISTINCT l.id) AS lesson_count,
                c.created_at AS created_at,
                c.updated_at AS updated_at
            FROM courses c
            LEFT JOIN course_modules m ON m.course_id = c.id
            LEFT JOIN lessons l ON l.module_id = m.id
            WHERE c.created_by = :ownerId
              AND c.is_deleted = false
              AND (
                  CAST(:keyword AS text) IS NULL
                  OR LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%'))
                  OR LOWER(c.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%'))
              )
              AND (CAST(:status AS text) IS NULL OR c.status = CAST(:status AS text))
              AND (:skillsCount = 0 OR
                   EXISTS (
                       SELECT 1 FROM unnest(c.skill_focus) AS skill
                       WHERE LOWER(skill) = ANY(
                           SELECT LOWER(unnest) FROM unnest(CAST(:skills AS text[]))
                       )
                   ))
            GROUP BY c.id, c.title, c.slug, c.description, c.language, c.thumbnail, c.skill_focus,
                     c.price_cents, c.currency, c.status, c.created_at, c.updated_at
            """;

        String countQuery = """
            SELECT COUNT(DISTINCT c.id)
            FROM courses c
            WHERE c.created_by = :ownerId
              AND c.is_deleted = false
              AND (
                  CAST(:keyword AS text) IS NULL
                  OR LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%'))
                  OR LOWER(c.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%'))
              )
              AND (CAST(:status AS text) IS NULL OR c.status = CAST(:status AS text))
              AND (:skillsCount = 0 OR
                   EXISTS (
                       SELECT 1 FROM unnest(c.skill_focus) AS skill
                       WHERE LOWER(skill) = ANY(
                           SELECT LOWER(unnest) FROM unnest(CAST(:skills AS text[]))
                       )
                   ))
            """;
        return executeQuery(baseQuery, countQuery, keyword, status, ownerId, skills, pageable);
    }

    private Page<CourseWithStatsProjection> executeQuery(
            String baseQuery,
            String countQuery,
            String keyword,
            String status,
            UUID ownerId,
            String[] skills,
            Pageable pageable
    ) {
        // Build ORDER BY clause from Pageable
        String orderByClause = buildOrderByClause(pageable.getSort());
        String fullQuery = baseQuery + orderByClause;

        // Execute main query
        Query query = entityManager.createNativeQuery(fullQuery);
        setParameters(query, keyword, status, ownerId, skills);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<CourseWithStatsProjection> content = results.stream()
                .map(this::mapToProjection)
                .collect(Collectors.toList());

        // Execute count query
        Query countQueryObj = entityManager.createNativeQuery(countQuery);
        setParameters(countQueryObj, keyword, status, ownerId, skills);
        long total = ((Number) countQueryObj.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }

    private String buildOrderByClause(Sort sort) {
        if (sort.isUnsorted()) {
            return " ORDER BY c.created_at DESC";
        }

        String orderBy = sort.stream()
                .map(order -> {
                    String column = SORT_COLUMN_MAP.getOrDefault(order.getProperty(), "c.created_at");
                    String direction = order.getDirection().isAscending() ? "ASC" : "DESC";
                    return column + " " + direction;
                })
                .collect(Collectors.joining(", "));

        return " ORDER BY " + orderBy;
    }

    private void setParameters(Query query, String keyword, String status, UUID ownerId, String[] skills) {
        // Unwrap to Hibernate NativeQuery for better type handling
        NativeQuery<?> nativeQuery = query.unwrap(NativeQuery.class);

        // Set parameters with proper type handling
        nativeQuery.setParameter("keyword", keyword);
        nativeQuery.setParameter("status", status);

        if (ownerId != null) {
            nativeQuery.setParameter("ownerId", ownerId);
        }

        int skillsCount = (skills == null || skills.length == 0) ? 0 : skills.length;
        nativeQuery.setParameter("skillsCount", skillsCount);

        // Set array parameter - Hibernate will handle PostgreSQL array type correctly
        String[] skillsParam = (skills == null || skills.length == 0) ? new String[0] : skills;
        nativeQuery.setParameter("skills", skillsParam);
    }

    private CourseWithStatsProjection mapToProjection(Object[] row) {
        return new CourseWithStatsProjection() {
            @Override
            public UUID getId() {
                return (UUID) row[0];
            }

            @Override
            public String getTitle() {
                return (String) row[1];
            }

            @Override
            public String getSlug() {
                return (String) row[2];
            }

            @Override
            public String getDescription() {
                return (String) row[3];
            }

            @Override
            public String getLanguage() {
                return (String) row[4];
            }

            @Override
            public String getThumbnail() {
                return (String) row[5];
            }

            @Override
            public String[] getSkillFocus() {
                Object skillFocus = row[6];
                if (skillFocus instanceof String[]) {
                    return (String[]) skillFocus;
                }
                return null;
            }

            @Override
            public Long getPriceCents() {
                Object priceCents = row[7];
                if (priceCents instanceof BigInteger) {
                    return ((BigInteger) priceCents).longValue();
                }
                return priceCents != null ? ((Number) priceCents).longValue() : null;
            }

            @Override
            public String getCurrency() {
                return (String) row[8];
            }

            @Override
            public String getStatus() {
                return (String) row[9];
            }

            @Override
            public Long getModuleCount() {
                Object count = row[10];
                if (count instanceof BigInteger) {
                    return ((BigInteger) count).longValue();
                }
                return count != null ? ((Number) count).longValue() : 0L;
            }

            @Override
            public Long getLessonCount() {
                Object count = row[11];
                if (count instanceof BigInteger) {
                    return ((BigInteger) count).longValue();
                }
                return count != null ? ((Number) count).longValue() : 0L;
            }

            @Override
            public Instant getCreatedAt() {
                Object timestamp = row[12];
                if (timestamp instanceof Timestamp) {
                    return ((Timestamp) timestamp).toInstant();
                }
                return (Instant) timestamp;
            }

            @Override
            public Instant getUpdatedAt() {
                Object timestamp = row[13];
                if (timestamp instanceof Timestamp) {
                    return ((Timestamp) timestamp).toInstant();
                }
                return (Instant) timestamp;
            }
        };
    }
}
