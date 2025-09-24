package com.english.api.common.exception;

/**
 * Created by hungpham on 9/22/2025
 */
public class CannotDeleteException extends RuntimeException {
    public CannotDeleteException(String message) {
        super(message);
    }
}
