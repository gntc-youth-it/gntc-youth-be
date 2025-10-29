package com.gntcyouthbe.bible.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VerseCopyResponse {

    private final String message;

    public static VerseCopyResponse success() {
        return new VerseCopyResponse("성경 구절 필사가 완료되었습니다.");
    }
}
