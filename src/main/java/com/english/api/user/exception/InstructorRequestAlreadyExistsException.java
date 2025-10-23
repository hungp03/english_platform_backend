package com.english.api.user.exception;

public class InstructorRequestAlreadyExistsException extends RuntimeException {
    public InstructorRequestAlreadyExistsException(String message) {
        super(message);
    }
}