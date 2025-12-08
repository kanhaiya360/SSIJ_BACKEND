package com.akeshya.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String path,
    String method,
    int status,
    String error,
    String message,
    String errorCode,
    LocalDateTime timestamp,
    List<ValidationError> validationErrors
) {
    public ErrorResponse(String path, String method, int status, String error, String message, String errorCode) {
        this(path, method, status, error, message, errorCode, LocalDateTime.now(), null);
    }
    
    public ErrorResponse(String path, String method, int status, String error, String message) {
        this(path, method, status, error, message, null, LocalDateTime.now(), null);
    }
}