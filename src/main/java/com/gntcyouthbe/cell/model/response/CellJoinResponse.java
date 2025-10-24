package com.gntcyouthbe.cell.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CellJoinResponse {
    private final String message;

    public static CellJoinResponse success() {
        return new CellJoinResponse("구역 가입이 성공적으로 완료되었습니다.");
    }
}
