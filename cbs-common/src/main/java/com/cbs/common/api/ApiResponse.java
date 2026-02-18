package com.cbs.common.api;

import java.time.Instant;

public record ApiResponse<T>(boolean success, String message, T data, Instant timestamp, String errorCode) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now(), null);
    }

    public static <T> ApiResponse<T> failure(String message) {
        return failure(null, message);
    }

    public static <T> ApiResponse<T> failure(String errorCode, String message) {
        return new ApiResponse<>(false, message, null, Instant.now(), errorCode);
    }
}
