package com.english.api.auth.util;

import org.springframework.http.ResponseCookie;

/**
 * Created by hungpham on 9/24/2025
 */
public class CookieUtil {
    public static ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true) // true when https
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("None") // None when https
                .build();
    }
}
