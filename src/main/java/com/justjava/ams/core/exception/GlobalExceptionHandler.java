package com.justjava.ams.core.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        HttpStatusCode statusCode = ex.getStatusCode();
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();

        return new ResponseEntity<>(
                buildError(message, errorCodeFor(statusCode), statusCode.value(), request, null),
                statusCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(buildError("Validation failed", "VALIDATION_ERROR",
                        HttpStatus.BAD_REQUEST.value(), request, fieldErrors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return new ResponseEntity<>(
                buildError("Access denied", "FORBIDDEN", HttpStatus.FORBIDDEN.value(), request, null),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            WebRequest request) {
        String message = resolveDataIntegrityMessage(ex);
        return new ResponseEntity<>(
                buildError(message, "CONFLICT", HttpStatus.CONFLICT.value(), request, null),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return ResponseEntity.badRequest()
                .body(buildError(ex.getMessage(), "BAD_REQUEST", HttpStatus.BAD_REQUEST.value(), request, null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        HttpStatus status = statusForRuntimeException(ex);

        return new ResponseEntity<>(
                buildError(ex.getMessage(), errorCodeFor(status), status.value(), request, null),
                status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(
                buildError("An unexpected error occurred", "INTERNAL_SERVER_ERROR",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(), request, null),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ApiErrorResponse buildError(
            String message,
            String errorCode,
            Integer status,
            WebRequest request,
            Map<String, String> fieldErrors) {
        return ApiErrorResponse.builder()
                .message(message != null ? message : "An unexpected error occurred")
                .errorCode(errorCode)
                .status(status)
                .path(resolvePath(request))
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();
    }

    private HttpStatus statusForRuntimeException(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (message.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }
        if (message.contains("already exists")
                || message.contains("duplicate")
                || message.contains("overlap")) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveDataIntegrityMessage(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return "Data integrity violation";
        }
        return message;
    }

    private String errorCodeFor(HttpStatusCode statusCode) {
        if (statusCode.isSameCodeAs(HttpStatus.BAD_REQUEST)) {
            return "BAD_REQUEST";
        }
        if (statusCode.isSameCodeAs(HttpStatus.NOT_FOUND)) {
            return "NOT_FOUND";
        }
        if (statusCode.isSameCodeAs(HttpStatus.CONFLICT)) {
            return "CONFLICT";
        }
        if (statusCode.isSameCodeAs(HttpStatus.FORBIDDEN)) {
            return "FORBIDDEN";
        }
        return statusCode.is5xxServerError() ? "INTERNAL_SERVER_ERROR" : "API_ERROR";
    }

    private String resolvePath(WebRequest request) {
        if (request == null) {
            return null;
        }
        String description = request.getDescription(false);
        return description != null && description.startsWith("uri=")
                ? description.substring(4)
                : description;
    }
}

