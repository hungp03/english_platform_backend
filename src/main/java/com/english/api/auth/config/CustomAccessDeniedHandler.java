package com.english.api.auth.config;

import com.english.api.common.dto.ApiResponse;
import com.english.api.common.util.constant.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value()); // 403
        response.setContentType("application/json;charset=UTF-8");
        String message = "Access Denied";
        ApiResponse<Object> apiResponse = ApiResponse.error(ErrorCode.FORBIDDEN, message);
        mapper.writeValue(response.getWriter(), apiResponse);
    }
}
