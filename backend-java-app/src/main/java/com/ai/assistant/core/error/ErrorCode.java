package com.ai.assistant.core.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INTERNAL_ERROR(
            "E-0001",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Unexpected error: %s",
            "An unexpected error occurred. Please try again later."
    ),

    INVALID_INPUT(
            "E-0002",
            HttpStatus.BAD_REQUEST,
            "Validation failed: %s",
            "There was a problem with your request: %s"
    ),

    USER_NOT_FOUND(
            "E-0003",
            HttpStatus.NOT_FOUND,
            "User not found '%s'",
            "The requested user does not exist."
    ),

    SERVICE_UNAVAILABLE(
            "E-0004",
            HttpStatus.SERVICE_UNAVAILABLE,
            "Circuit breaker open: %s",
            "Service temporarily unavailable."
    ),

    TOO_MANY_REQUESTS(
            "E-0005",
            HttpStatus.TOO_MANY_REQUESTS,
            "Bulkhead full: %s",
            "Too many requests, please try later."
    ),

    TIMEOUT_EXCEEDED(
            "E-0006",
            HttpStatus.GATEWAY_TIMEOUT,
            "Timeout occurred: %s",
            "Request took too long, please try again later."
    ),

    INVALID_CURRENT_USER_TYPE(
            "E-0007",
            HttpStatus.BAD_REQUEST,
            "Unsupported @CurrentUser parameter type: %s",
            "Invalid parameter usage."
    ),

    INCORRECT_CREDENTIALS(
            "E-0008",
            HttpStatus.UNAUTHORIZED,
            "Incorrect credentials provided for login attempt",
            "Incorrect password."
    ),

    USERNAME_ALREADY_EXISTS(
            "E-0009",
            HttpStatus.CONFLICT,
            "Username already in use.",
            "Username already in use."
    ),

    EMAIL_ALREADY_EXISTS(
            "E-0010",
            HttpStatus.CONFLICT,
            "Email already in use.",
            "Email already in use."
    ),

    PASSWORD_NOT_MATCH(
            "E-0011",
            HttpStatus.BAD_REQUEST,
            "Passwords do not match.",
            "Passwords do not match."
    ),

    MALFORMED_REQUEST(
            "E-0012",
            HttpStatus.BAD_REQUEST,
            "Failed to read HTTP message: %s",
            "Malformed request payload."
    ),

    METHOD_NOT_ALLOWED(
            "E-0013",
            HttpStatus.METHOD_NOT_ALLOWED,
            "Method not allowed: %s",
            "Method not allowed."
    ),

    NO_RESOURCE_FOUND(
            "E-0014",
            HttpStatus.NOT_FOUND,
            "No resource found: %s",
            "No resource found."
    ),

    UNSUPPORTED_MEDIA_TYPE(
            "E-0015",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Missing content type header: %s",
            "Missing content type header."
    ),

    NOT_ACCEPTABLE(
            "E-0016",
            HttpStatus.NOT_ACCEPTABLE,
            "Could not find acceptable representation: %s",
            "Could not find acceptable representation."
    ),

    INVALID_TOKEN(
            "E-0017",
            HttpStatus.UNAUTHORIZED,
            "Invalid token: %s",
            "Invalid token."
    ),

    NOT_FOUND(
            "E-0018",
            HttpStatus.NOT_FOUND,
            "Not found: %s",
            "Not found: %s"
    ),

    UNAUTHORIZED(
            "E-0019",
            HttpStatus.UNAUTHORIZED,
            "Unauthorized: %s",
            "Unauthorized."
    ),

    BAD_CREDENTIALS(
            "E-0020",
            HttpStatus.UNAUTHORIZED,
            "Bad credentials.",
            "Invalid credentials."
    ),

    JSON_PROCESSING_EXCEPTION(
            "E-0021",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Could not proceed to json: %s",
            "Unexpected error occurred. Please try again later."
    ),

    KAFKA_PUBLISH_ERROR(
            "E-0022",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Could not publish message to kafka: %s",
            "Unexpected error occurred. Please try again later."
    );


    private final String code;
    private final HttpStatus httpStatus;
    private final String devMessageTemplate;
    private final String userMessageTemplate;

    ErrorCode(String code,
              HttpStatus httpStatus,
              String devMessageTemplate,
              String userMessageTemplate) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.devMessageTemplate = devMessageTemplate;
        this.userMessageTemplate = userMessageTemplate;
    }

    public String formatDevMessage(Object... args) {
        return String.format(devMessageTemplate, args);
    }

    public String formatUserMessage(Object... args) {
        return String.format(userMessageTemplate, args);
    }
}
