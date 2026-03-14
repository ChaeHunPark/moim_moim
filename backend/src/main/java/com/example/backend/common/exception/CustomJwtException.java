package com.example.backend.common.exception;

import lombok.Getter;

@Getter
public class CustomJwtException extends RuntimeException {

    private final String errorCode;

    public CustomJwtException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
