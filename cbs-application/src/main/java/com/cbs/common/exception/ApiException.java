package com.cbs.common.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public ApiException(String errorCode, String message) {
        this(errorCode, message, HttpStatus.BAD_REQUEST);
    }

    public ApiException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
