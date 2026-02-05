package com.gntcyouthbe.user.model.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserNameUpdateResponse {

    private final String message;

    public static UserNameUpdateResponse success() {
        return new UserNameUpdateResponse("이름 변경이 성공적으로 완료되었습니다.");
    }
}
