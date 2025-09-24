package com.english.api.common.util;

import com.english.api.common.dto.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Created by hungpham on 9/22/2025
 */
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
        int statusCode = response instanceof org.springframework.http.server.ServletServerHttpResponse
                ? ((org.springframework.http.server.ServletServerHttpResponse) response).getServletResponse().getStatus()
                : HttpStatus.OK.value();

        if (body instanceof ApiResponse<?> || body instanceof String || body instanceof Resource || body instanceof byte[]) {
            return body;
        }

        if (statusCode >= 400) {
            return body;
        }

        return new ApiResponse<>(
                true,
                statusCode,
                "OK",
                body
        );
    }
}

