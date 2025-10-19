package com.english.api.course.repository.custom;

import com.english.api.course.dto.projection.CourseWithStatsProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
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
        SORT_COLUMN_MAP.put("published", "c.is_published");
        SORT_COLUMN_MAP.put("moduleCount", "module_count");
        SORT_COLUMN_MAP.put("lessonCount", "lesson_count");
    }

    @Override
    public Page<CourseWithStatsProjection> searchWithStats(
            String keyword,
            Boolean isPublished,
            String[] skills,
            Pageable pageable
    ) {
        String baseQuery = """
            SELECT c.id AS id,
                c.title AS title,
                c.description AS description,
                c.language AS language,
                c.thumbnail AS thumbnail,
                c.skill_focus AS skill_focus,
                c.price_cents AS price_cents,
                c.currency AS currency,
                c.is_published AS is_published,
                COUNT(DISTINCT m.id) AS module_count,
                COUNT(DISTINCT l.id) AS lesson_count,
                c.created_at AS created_at,
                c.updated_at AS updated_at
            FROM courses c
            LEFT JOIN course_modules m ON m.course_id = c.id
            LEFT JOIN lessons l ON l.module_id = m.id
            WHERE (:keyword IS NULL OR
                   LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:isPublished IS NULL OR c.is_published = :isPublished)
              AND (:skillsCount = 0 OR
                   EXISTS (
                       SELECT 1 FROM unnest(c.skill_focus) AS skill
                       WHERE LOWER(skill) = ANY(
                           SELECT LOWER(unnest) FROM unnest(CAST(:skills AS text[]))
                       )
                   ))
            GROUP BY c.id, c.title, c.description, c.language, c.thumbnail, c.skill_focus,
                     c.price_cents, c.currency, c.is_published, c.created_at, c.updated_at
            """;

        String countQuery = """
            SELECT COUNT(DISTINCT c.id)
            FROM courses c
            WHERE (:keyword IS NULL OR
                   LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:isPublished IS NULL OR c.is_published = :isPublished)
              AND (:skillsCount = 0 OR
                   EXISTS (
                       SELECT 1 FROM unnest(c.skill_focus) AS skill
                       WHERE LOWER(skill) = ANY(
                           SELECT LOWER(unnest) FROM unnest(CAST(:skills AS text[]))
                       )
                   ))
            """;

        return executeQuery(baseQuery, countQuery, keyword, isPublished, null, skills, pageable);
    }

    @Override
    public Page<CourseWithStatsProjection> searchByOwnerWithStats(
            UUID ownerId,
            String keyword,
            Boolean isPublished,
            String[] skills,
            Pageable pageable
    ) {
        String baseQuery = """
            SELECT c.id AS id,
                c.title AS title,
                c.description AS description,
                c.language AS language,
                c.thumbnail AS thumbnail,
                c.skill_focus AS skill_focus,
                c.price_cents AS price_cents,
                c.currency AS currency,
                c.is_published AS is_published,
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
                  :keyword IS NULL
                  OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
              AND (:isPublished IS NULL OR c.is_published = :isPublished)
              AND (:skillsCount = 0 OR
                   EXISTS (
                       SELECT 1 FROM unnest(c.skill_focus) AS skill
                       WHERE LOWER(skill) = ANY(
                           SELECT LOWER(unnest) FROM unnest(CAST(:skills AS text[]))
                       )
                   ))
            GROUP BY c.id, c.title, c.description, c.language, c.thumbnail, c.skill_focus,
                     c.price_cents, c.currency, c.is_published, c.created_at, c.updated_at
            """;

        String countQuery = """
            SELECT COUNT(DISTINCT c.id)
            FROM courses c
            WHERE c.created_by = :ownerId
              AND c.is_deleted = false
              AND (
                  :keyword IS NULL
                  OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
              AND (:isPublished IS NULL OR c.is_published = :isPublished)
              AND (:skillsCount = 0 OR
                   EXISTS (
                       SELECT 1 FROM unnest(c.skill_focus) AS skill
                       WHERE LOWER(skill) = ANY(
                           SELECT LOWER(unnest) FROM unnest(CAST(:skills AS text[]))
                       )
                   ))
            """;

        return executeQuery(baseQuery, countQuery, keyword, isPublished, ownerId, skills, pageable);
    }

    private Page<CourseWithStatsProjection> executeQuery(
            String baseQuery,
            String countQuery,
            String keyword,
            Boolean isPublished,
            UUID ownerId,
            String[] skills,
            Pageable pageable
    ) {
        // Build ORDER BY clause from Pageable
        String orderByClause = buildOrderByClause(pageable.getSort());
        String fullQuery = baseQuery + orderByClause;

        // Execute main query
        Query query = entityManager.createNativeQuery(fullQuery);
        setParameters(query, keyword, isPublished, ownerId, skills);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<CourseWithStatsProjection> content = results.stream()
                .map(this::mapToProjection)
                .collect(Collectors.toList());

        // Execute count query
        Query countQueryObj = entityManager.createNativeQuery(countQuery);
        setParameters(countQueryObj, keyword, isPublished, ownerId, skills);
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

    private void setParameters(Query query, String keyword, Boolean isPublished, UUID ownerId, String[] skills) {
        query.setParameter("keyword", keyword);
        query.setParameter("isPublished", isPublished);
        if (ownerId != null) {
            query.setParameter("ownerId", ownerId);
        }
        query.setParameter("skillsCount", skills == null ? 0 : skills.length);
        query.setParameter("skills", skills == null ? new String[0] : skills);
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
            public String getDescription() {
                return (String) row[2];
            }

            @Override
            public String getLanguage() {
                return (String) row[3];
            }

            @Override
            public String getThumbnail() {
                return (String) row[4];
            }

            @Override
            public String[] getSkillFocus() {
                Object skillFocus = row[5];
                if (skillFocus instanceof String[]) {
                    return (String[]) skillFocus;
                }
                return null;
            }

            @Override
            public Long getPriceCents() {
                Object priceCents = row[6];
                if (priceCents instanceof BigInteger) {
                    return ((BigInteger) priceCents).longValue();
                }
                return priceCents != null ? ((Number) priceCents).longValue() : null;
            }

            @Override
            public String getCurrency() {
                return (String) row[7];
            }

            @Override
            public Boolean getIsPublished() {
                return (Boolean) row[8];
            }

            @Override
            public Long getModuleCount() {
                Object count = row[9];
                if (count instanceof BigInteger) {
                    return ((BigInteger) count).longValue();
                }
                return count != null ? ((Number) count).longValue() : 0L;
            }

            @Override
            public Long getLessonCount() {
                Object count = row[10];
                if (count instanceof BigInteger) {
                    return ((BigInteger) count).longValue();
                }
                return count != null ? ((Number) count).longValue() : 0L;
            }

            @Override
            public Instant getCreatedAt() {
                Object timestamp = row[11];
                if (timestamp instanceof Timestamp) {
                    return ((Timestamp) timestamp).toInstant();
                }
                return (Instant) timestamp;
            }

            @Override
            public Instant getUpdatedAt() {
                Object timestamp = row[12];
                if (timestamp instanceof Timestamp) {
                    return ((Timestamp) timestamp).toInstant();
                }
                return (Instant) timestamp;
            }
        };
    }
}
