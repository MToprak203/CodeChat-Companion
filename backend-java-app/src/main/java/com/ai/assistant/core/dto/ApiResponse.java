package com.ai.assistant.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Generic API response wrapper for all endpoints.
 *
 * @param <T> Type of the successful response payload
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    /** Indicates whether the request was processed successfully */
    private boolean success;

    /** The actual payload in case of success */
    private T data;

    /** The error payload in case of failure */
    private ErrorResponse error;

    /** Optional metadata (e.g. paging info) */
    private Map<String, Object> meta;

    /**
     * Create a successful response without metadata.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Create a successful response with metadata.
     */
    public static <T> ApiResponse<T> success(T data, Map<String, Object> meta) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .meta(meta)
                .build();
    }

    /**
     * Create a failure response wrapping an existing ErrorResponse.
     */
    public static <T> ApiResponse<T> failure(ErrorResponse error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .build();
    }
}