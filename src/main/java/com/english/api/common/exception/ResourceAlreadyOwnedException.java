package com.english.api.common.exception;

/**
 * Exception thrown when user tries to purchase or access a resource they already own
 * This is different from ResourceAlreadyExistsException which is for general duplication
 */
public class ResourceAlreadyOwnedException extends RuntimeException {

    public ResourceAlreadyOwnedException(String message) {
        super(message);
    }

    public ResourceAlreadyOwnedException(String message, Throwable cause) {
        super(message, cause);
    }
}