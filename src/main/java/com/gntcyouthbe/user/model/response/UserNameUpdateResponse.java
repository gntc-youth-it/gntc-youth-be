package com.gntcyouthbe.user.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.cell.model.response.CellJoinResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserNameUpdateResponse {

    private final String message;

    public static UserNameUpdateResponse success() {
        return new UserNameUpdateResponse("이름 변경이 성공적으로 완료되었습니다.");
    }
}
