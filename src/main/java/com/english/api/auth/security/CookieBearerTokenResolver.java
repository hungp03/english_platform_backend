package com.english.api.auth.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * Created by hungpham on 9/24/2025
 */
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private final String cookieName;
    private final List<String> whiteList;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public CookieBearerTokenResolver(String cookieName, List<String> whiteList) {
        this.cookieName = cookieName;
        this.whiteList = whiteList;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Nếu request khớp whitelist → bỏ qua token
        for (String wl : whiteList) {
            if (pathMatcher.match(wl, path)) {
                return null;
            }
        }

        // Lấy token từ cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
