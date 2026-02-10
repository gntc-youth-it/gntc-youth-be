package com.gntcyouthbe.common.exception;

import com.gntcyouthbe.common.exception.model.ExceptionCode;
import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {

    private final int code;

    public ForbiddenException(final ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.code = exceptionCode.getCode();
    }
}
