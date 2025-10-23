package com.gntcyouthbe.common.exception.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExceptionCode {

    INVALID_REQUEST(1001, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(9999, "서버에서 알 수 없는 오류가 발생했습니다.");

    private final int code;
    private final String message;
}
