package com.english.api.common.exception;

/**
 * Created by hungpham on 9/22/2025
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
