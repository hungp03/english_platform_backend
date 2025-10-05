package com.english.api.common.exception;

import com.english.api.common.dto.ApiResponse;
import com.english.api.common.util.constant.ErrorCode;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by hungpham on 9/22/2025
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Object>> handleCredentialException(RuntimeException ex) {
        return buildResponse(ErrorCode.BAD_CREDENTIALS, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorizedException(UnauthorizedException ex) {
        return buildResponse(ErrorCode.UNAUTHORIZED, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        return buildResponse(ErrorCode.FORBIDDEN, ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceInvalidException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceInvalidException(ResourceInvalidException ex) {
        return buildResponse(ErrorCode.RESOURCE_INVALID, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildResponse(ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        return buildResponse(ErrorCode.RESOURCE_ALREADY_EXISTS, ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<String> errors = result.getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        String errorMessage = errors.size() > 1 ? String.join(", ", errors) : errors.getFirst();
        return buildResponse(ErrorCode.METHOD_NOT_VALID, errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(NoResourceFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND.value(),
                "404 Not Found. URL may not exist... " + ex.getMessage(),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CannotDeleteException.class)
    public ResponseEntity<ApiResponse<Object>> handleCannotDeleteException(CannotDeleteException ex) {
        return buildResponse(ErrorCode.CANNOT_DELETE, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<ApiResponse<Object>> handleOperationNotAllowedException(OperationNotAllowedException ex) {
        return buildResponse(ErrorCode.OPERATION_NOT_ALLOWED, ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DuplicatePositionException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicatePositionException(DuplicatePositionException ex) {
        return buildResponse(ErrorCode.DUPLICATE_KEY, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        return buildResponse(ErrorCode.EXCEPTION, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Object>> buildResponse(int statusCode, String message, HttpStatus status) {
        ApiResponse<Object> response = ApiResponse.error(statusCode, message);
        return ResponseEntity.status(status).body(response);
    }
}
