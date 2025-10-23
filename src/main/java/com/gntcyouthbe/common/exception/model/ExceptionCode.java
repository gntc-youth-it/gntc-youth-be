package com.gntcyouthbe.common.exception.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExceptionCode {

    INVALID_REQUEST(1001, "잘못된 요청입니다."),

    // 12xx: 구역 관련 예외
    CELL_MEMBER_NOT_FOUND(1202, "해당 구역원이 존재하지 않습니다."),
    CELL_GOAL_NOT_FOUND(1203, "해당 구역 목표가 존재하지 않습니다."),

    INTERNAL_SERVER_ERROR(9999, "서버에서 알 수 없는 오류가 발생했습니다.");

    private final int code;
    private final String message;
}
