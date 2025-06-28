package com.ai.assistant.core.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ValidationErrorResponse.class, name = "ValidationErrorResponse")
})
public class ErrorResponse {
    private final String code;
    private final String message;
    private Instant timestamp = Instant.now();
    private final String path;
}
