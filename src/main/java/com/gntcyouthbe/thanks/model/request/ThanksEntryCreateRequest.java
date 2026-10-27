package com.gntcyouthbe.thanks.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 날짜와 칸은 받지 않는다. 서버가 요청을 받은 시각으로 정한다.
public record ThanksEntryCreateRequest(
        @NotBlank(message = "감사제목을 입력해 주세요.")
        @Size(max = 80, message = "감사제목은 80자까지 쓸 수 있어요.")
        String content,

        // 파일이 없을 때도 FE가 같은 코드로 처리하도록 1001이 아니라 3001(FILE_NOT_FOUND)로 응답한다
        Long fileId
) {

    // 검증 전에 앞뒤 공백을 지워서, 공백만 있는 감사제목을 걸러내고 공백을 뺀 길이로 80자를 센다
    public ThanksEntryCreateRequest {
        if (content != null) {
            content = content.strip();
        }
    }
}
