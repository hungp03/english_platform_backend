package com.english.api.common.exception;

/**
 * Created by hungpham on 9/22/2025
 */
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
