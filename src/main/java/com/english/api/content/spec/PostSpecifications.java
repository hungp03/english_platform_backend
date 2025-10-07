package com.english.api.content.spec;

import com.english.api.content.model.ContentCategory;
import com.english.api.content.model.ContentPost;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

public class PostSpecifications {

    public static Specification<ContentPost> publishedOnly(boolean publishedOnly) {
        return (root, query, cb) -> publishedOnly ? cb.isTrue(root.get("published")) : cb.conjunction();
    }

    public static Specification<ContentPost> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("bodyMd")), like)
            );
        };
    }

    public static Specification<ContentPost> author(UUID authorId) {
        return (root, query, cb) -> {
            if (authorId == null) return cb.conjunction();
            return cb.equal(root.get("author").get("id"), authorId);
        };
    }

    public static Specification<ContentPost> categoryId(UUID categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return cb.conjunction();
            var join = root.join("categories");
            return cb.equal(join.get("id"), categoryId);
        };
    }

    public static Specification<ContentPost> categorySlug(String categorySlug) {
        return (root, query, cb) -> {
            if (categorySlug == null || categorySlug.isBlank()) return cb.conjunction();
            var join = root.join("categories");
            return cb.equal(cb.lower(join.get("slug")), categorySlug.toLowerCase());
        };
    }

    public static Specification<ContentPost> dateRange(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            Instant fromI = (from != null) ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
            Instant toI = (to != null) ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;
            if (fromI != null && toI != null) {
                return cb.between(root.get("publishedAt"), fromI, toI);
            } else if (fromI != null) {
                return cb.greaterThanOrEqualTo(root.get("publishedAt"), fromI);
            } else {
                return cb.lessThan(root.get("publishedAt"), toI);
            }
        };
    }
}