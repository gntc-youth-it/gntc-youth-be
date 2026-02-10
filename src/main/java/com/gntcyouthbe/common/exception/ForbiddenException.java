package com.gntcyouthbe.common.exception;

import com.gntcyouthbe.common.exception.model.ExceptionCode;
import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {

    private final int code;
    private final String message;

    public ForbiddenException(final ExceptionCode exceptionCode) {
        this.code = exceptionCode.getCode();
        this.message = exceptionCode.getMessage();
    }
}
