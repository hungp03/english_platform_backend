package com.english.api.auth.filter;
/**
 * Created by hungpham on 9/24/2025
 */

import com.english.api.common.dto.ApiResponse;
import com.english.api.common.util.constant.ErrorCode;
import com.english.api.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
public class AccountLockFilter extends OncePerRequestFilter {
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            try {
                UUID userId = UUID.fromString(authentication.getName());
                if (!userService.isUserActive(userId)) {
                    SecurityContextHolder.clearContext();
                    ApiResponse<Object> apiResponse = ApiResponse.error(ErrorCode.FORBIDDEN, "Account is locked");
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                    return;
                }
            } catch (IllegalArgumentException e) {
                // If subject is not a valid UUID → ignore, let other filters handle
            }
        }
        filterChain.doFilter(request, response);
    }
}