package com.english.api.content.util;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtil {
    private SlugUtil() {}

    // Normalize to a-lowercase, digits, dash. Collapse multi-dashes.
    public static String toSlug(String input) {
        if (input == null) return null;
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        return slug;
    }

    public static boolean isSeoFriendly(String slug) {
        if (slug == null) return false;
        return slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    }
}