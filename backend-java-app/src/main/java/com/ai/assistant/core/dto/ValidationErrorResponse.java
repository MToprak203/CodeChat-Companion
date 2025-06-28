package com.ai.assistant.core.dto;

import com.ai.assistant.core.error.FieldValidationError;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@JsonTypeName("ValidationErrorResponse")
public class ValidationErrorResponse extends ErrorResponse {
    private List<FieldValidationError> errors;

    public ValidationErrorResponse(String code, String message, Instant timestamp, String path, List<FieldValidationError> errors) {
        super(code, message, timestamp, path);
        this.errors = errors;
    }
    public ValidationErrorResponse(String code, String message, String path, List<FieldValidationError> errors) {
        super(code, message, path);
        this.errors = errors;
    }
}
