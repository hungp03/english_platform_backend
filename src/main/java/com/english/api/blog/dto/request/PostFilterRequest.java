package com.english.api.blog.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

public record PostFilterRequest(
        String keyword,
        UUID authorId,
        UUID categoryId,
        String categorySlug,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fromDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate toDate
) {
}
