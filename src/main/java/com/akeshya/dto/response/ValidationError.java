package com.akeshya.dto.response;

public record ValidationError(
    String field,
    String message,
    Object rejectedValue
) {}