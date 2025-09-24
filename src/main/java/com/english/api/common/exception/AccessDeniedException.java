package com.english.api.common.exception;

/**
 * Created by hungpham on 9/22/2025
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
