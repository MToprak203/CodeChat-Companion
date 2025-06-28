package com.ai.assistant.core.error;

import com.ai.assistant.core.dto.ApiResponse;
import com.ai.assistant.core.dto.ErrorResponse;
import com.ai.assistant.core.dto.ValidationErrorResponse;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Global exception handler adapted for Spring WebFlux (reactive) endpoints.
 */
@Slf4j
@RestControllerAdvice
public class GlobalReactiveExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleApplicationException(
            ApplicationException ex,
            ServerWebExchange exchange) {

        ErrorCode code = ex.getErrorCode();

        log.error("ErrorCode={} | {}",
                code.getCode(),
                code.formatDevMessage(ex.getParams()),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                code.getCode(),
                code.formatUserMessage(ex.getParams()),
                exchange.getRequest().getPath().value()
        );

        ApiResponse<Void> response = ApiResponse.failure(error);
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(response));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleValidationException(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {

        List<FieldValidationError> fieldErrors = ex.getFieldErrors().stream()
                .map(err -> new FieldValidationError(
                        err.getField(),
                        err.getDefaultMessage()))
                .collect(Collectors.toList());

        String summary = fieldErrors.stream()
                .map(e -> e.getField() + ": " + e.getMessage())
                .collect(Collectors.joining("; "));

        ErrorCode code = ErrorCode.INVALID_INPUT;

        log.warn("Validation failed [{}]: {}",
                exchange.getRequest().getPath().value(),
                code.formatDevMessage(summary),
                ex
        );

        ValidationErrorResponse error = new ValidationErrorResponse(
                code.getCode(),
                code.formatUserMessage(summary),
                exchange.getRequest().getPath().value(),
                fieldErrors
        );

        ApiResponse<Void> response = ApiResponse.failure(error);
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(response));
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleCircuitBreakerOpen(
            CallNotPermittedException ex,
            ServerWebExchange exchange) {

        ErrorCode code = ErrorCode.SERVICE_UNAVAILABLE;

        log.error("CircuitBreaker OPEN on {}: {}",
                exchange.getRequest().getPath().value(),
                code.formatDevMessage(ex.getMessage()),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                code.getCode(),
                code.formatUserMessage(),
                exchange.getRequest().getPath().value()
        );

        ApiResponse<Void> response = ApiResponse.failure(error);
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(response));
    }

    @ExceptionHandler(BulkheadFullException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleBulkheadFull(
            BulkheadFullException ex,
            ServerWebExchange exchange) {

        ErrorCode code = ErrorCode.TOO_MANY_REQUESTS;

        log.error("Bulkhead FULL on {}: {}",
                exchange.getRequest().getPath().value(),
                code.formatDevMessage(ex.getMessage()),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                code.getCode(),
                code.formatUserMessage(),
                exchange.getRequest().getPath().value()
        );

        ApiResponse<Void> response = ApiResponse.failure(error);
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(response));
    }

    @ExceptionHandler(TimeoutException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleTimeout(
            TimeoutException ex,
            ServerWebExchange exchange) {

        ErrorCode code = ErrorCode.TIMEOUT_EXCEEDED;

        log.error("Timeout in {}: {}",
                exchange.getRequest().getPath().value(),
                code.formatDevMessage(ex.getMessage()),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                code.getCode(),
                code.formatUserMessage(),
                exchange.getRequest().getPath().value()
        );

        ApiResponse<Void> response = ApiResponse.failure(error);
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(response));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleJsonParseException(ServerWebInputException ex, ServerWebExchange exchange) {

        ErrorCode code = ErrorCode.MALFORMED_REQUEST;

        log.warn("Server web input exception in {}: {}",
                exchange.getRequest().getPath().value(),
                code.formatDevMessage(ex.getMessage()),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                code.getCode(),
                code.formatUserMessage(),
                exchange.getRequest().getPath().value()
        );

        ApiResponse<Void> response = ApiResponse.failure(error);
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(response));
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleMethodNotAllowedException(MethodNotAllowedException ex, ServerWebExchange exchange) {
        ErrorCode code = ErrorCode.METHOD_NOT_ALLOWED;

        log.warn("Method not allowed exception in {}: {}",
                exchange.getRequest().getPath().value(),
                code.formatDevMessage(ex.getMessage()),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                code.getCode(),
                code.formatUserMessage(),
                exchange.getRequest().getPath().value()
        );

        ApiResponse<Void> response = ApiResponse.failure(error);
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(response));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleNoResourceFoundException(NoResourceFoundException ex, ServerWebExchange exchange) {
        ErrorCode code = ErrorCode.NO_RESOURCE_FOUND;

        log.warn("No resource found exception in {} : {}",
                exchange.getRequest().getPath().value(),
                code.formatDevMessage(ex.getMessage()),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                code.getCode(),
                code.formatUserMessage(),
                exchange.getRequest().getPath().value()
        );

        ApiResponse<Void> response = ApiResponse.failure(error);
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(response));
    }

    @ExceptionHandler(NotAcceptableStatusException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleNotAcceptableStatusException(NotAcceptableStatusException ex, ServerWebExchange exchange) {
        ErrorCode code = ErrorCode.NOT_ACCEPTABLE;

        log.warn("Not acceptable status exception in {} : {}",
                exchange.getRequest().getPath().value(),
                code.formatDevMessage(ex.getMessage()),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                code.getCode(),
                code.getUserMessageTemplate(),
                exchange.getRequest().getPath().value()
        );

        ApiResponse<Void> response = ApiResponse.failure(error);
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(response));
    }

    @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleUnsupportedMediaTypeStatusException(UnsupportedMediaTypeStatusException ex, ServerWebExchange exchange) {
        ErrorCode code = ErrorCode.UNSUPPORTED_MEDIA_TYPE;

        log.warn("Unsupported media type status exception in {} : {}",
                exchange.getRequest().getPath().value(),
                code.formatDevMessage(ex.getMessage()),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                code.getCode(),
                code.getUserMessageTemplate(),
                exchange.getRequest().getPath().value()
        );

        ApiResponse<Void> response = ApiResponse.failure(error);
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(response));
    }

    @ExceptionHandler(JwtException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleJwtException(JwtException ex, ServerWebExchange exchange) {
        ErrorCode code = ErrorCode.INVALID_TOKEN;

        log.warn("JWT exception in {} : {}",
                exchange.getRequest().getPath().value(),
                code.formatDevMessage(ex.getMessage()),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                code.getCode(),
                code.formatUserMessage(),
                exchange.getRequest().getPath().value()
        );

        ApiResponse<Void> response = ApiResponse.failure(error);
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(response));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleBadCredentialsException(BadCredentialsException ex, ServerWebExchange exchange) {
        ErrorCode code = ErrorCode.BAD_CREDENTIALS;

        log.debug("Bad credentials exception in {} : {}",
                exchange.getRequest().getPath().value(),
                code.formatDevMessage(),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                code.getCode(),
                code.formatUserMessage(),
                exchange.getRequest().getPath().value()
        );

        ApiResponse<Void> response = ApiResponse.failure(error);
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleAllExceptions(
            Exception ex,
            ServerWebExchange exchange) {

        ErrorCode code = ErrorCode.INTERNAL_ERROR;

        log.error("Unhandled exception in {} : {}",
                exchange.getRequest().getPath().value(),
                code.formatDevMessage(ex.getMessage()),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                code.getCode(),
                code.formatUserMessage(),

                exchange.getRequest().getPath().value()
        );

        ApiResponse<Void> response = ApiResponse.failure(error);
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(response));
    }
}