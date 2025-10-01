package com.english.api.common.exception;

/**
 * Created by hungpham on 10/1/2025
 */
public class OperationNotAllowedException extends RuntimeException {
    public OperationNotAllowedException(String message) {
        super(message);
    }
}
