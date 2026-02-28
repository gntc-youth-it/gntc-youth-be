package com.gntcyouthbe.common.exception.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExceptionCode {

    INVALID_REQUEST(1001, "잘못된 요청입니다."),
    UNAUTHORIZED(1002, "인증이 필요합니다."),

    // 11xx: 회원 관련 예외
    USER_NOT_FOUND(1101, "해당 사용자가 존재하지 않습니다."),
    USER_PROFILE_NOT_FOUND(1102, "해당 사용자의 프로필이 존재하지 않습니다."),
    USER_NO_CHURCH(1103, "성전이 지정되지 않은 사용자는 회장으로 임명할 수 없습니다."),
    INVALID_ROLE(1104, "유효하지 않은 권한입니다."),
    SAME_ROLE(1105, "이미 해당 권한을 가지고 있습니다."),

    // 12xx: 구역 관련 예외
    CELL_NOT_FOUND(1201, "해당 구역이 존재하지 않습니다."),
    CELL_MEMBER_NOT_FOUND(1202, "해당 구역원이 존재하지 않습니다."),
    CELL_GOAL_NOT_FOUND(1203, "해당 구역 목표가 존재하지 않습니다."),

    // 20xx: 성경 관련 예외
    BOOK_NOT_FOUND(2001, "해당 성경 책이 존재하지 않습니다."),
    CHAPTER_NOT_FOUND(2002, "해당 장이 존재하지 않습니다."),
    VERSE_NOT_FOUND(2003, "해당 성경 구절이 존재하지 않습니다."),
    VERSE_COPY_NOT_FOUND(2004, "해당 성경 필사 기록이 존재하지 않습니다."),

    // 21xx: 어드벤트 관련 예외
    ADVENT_PERSON_NOT_FOUND(2101, "해당 어드벤트 사용자가 존재하지 않습니다."),

    // 30xx: 파일 관련 예외
    FILE_NOT_FOUND(3001, "해당 파일이 존재하지 않습니다."),

    // 31xx: 성전 정보 관련 예외
    CHURCH_NOT_FOUND(3100, "해당 성전이 존재하지 않습니다."),
    CHURCH_INFO_NOT_FOUND(3101, "해당 성전 정보가 존재하지 않습니다."),
    CHURCH_ACCESS_DENIED(3102, "해당 성전에 대한 수정 권한이 없습니다."),

    // 40xx: 게시글 관련 예외
    POST_NOT_FOUND(4001, "해당 게시글이 존재하지 않습니다."),
    POST_ACCESS_DENIED(4002, "해당 게시글에 대한 권한이 없습니다."),
    POST_NOT_PENDING_REVIEW(4003, "검수대기 상태의 게시글만 승인할 수 있습니다."),

    INTERNAL_SERVER_ERROR(9999, "서버에서 알 수 없는 오류가 발생했습니다.");

    private final int code;
    private final String message;
}
