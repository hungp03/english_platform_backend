package com.english.api.forum.util;
import java.text.Normalizer;
import java.util.Locale;
public class SlugUtil {
  public static String slugify(String input) {
    if (input == null) return "";
    String nowhitespace = input.trim().replaceAll("\\s+", "-");
    String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
    String slug = normalized.replaceAll("[^\\w-]", "").toLowerCase(Locale.ROOT);
    slug = slug.replaceAll("-{2,}", "-");
    return slug.replaceAll("^-|-$", "");
  }
  public static String ensureUnique(String base, java.util.function.Predicate<String> exists) {
    String s = slugify(base);
    if (!exists.test(s)) return s;
    for (int i = 2; i < 9999; i++) {
      String cand = s + "-" + i;
      if (!exists.test(cand)) return cand;
    }
    return s + "-" + System.currentTimeMillis();
  }
}
