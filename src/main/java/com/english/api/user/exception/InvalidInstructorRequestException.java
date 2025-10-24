package com.english.api.user.exception;

public class InvalidInstructorRequestException extends RuntimeException {
    public InvalidInstructorRequestException(String message) {
        super(message);
    }
}