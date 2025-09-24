package com.english.api.common.exception;

/**
 * Created by hungpham on 9/22/2025
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
