package com.ai.assistant.core.error;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object[] params;

    public ApplicationException(ErrorCode errorCode, Object... params) {
        super(errorCode.formatDevMessage(params));
        this.errorCode = errorCode;
        this.params = params;
    }

    public ApplicationException(ErrorCode errorCode, Throwable cause, Object... params) {
        super(errorCode.formatDevMessage(params), cause);
        this.errorCode = errorCode;
        this.params = params;
    }
}