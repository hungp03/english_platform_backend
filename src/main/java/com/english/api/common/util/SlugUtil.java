package com.english.api.common.util;

import java.text.Normalizer;
import java.util.UUID;

/**
 * Created by hungpham on 10/2/2025
 */
public final class SlugUtil {
    private SlugUtil() {}

    public static String toSlugWithUuid(String title) {
        String base = toSlug(title);
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return base.isEmpty() ? suffix : base + "-" + suffix;
    }

    public static String toSlug(String input) {
        if (input == null) return "";
        String s = input.trim().toLowerCase();

        s = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        s = s.replace('đ', 'd').replace('Đ', 'd');

        s = s.replaceAll("[^a-z0-9]+", "-");

        s = s.replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");

        return s;
    }
}
