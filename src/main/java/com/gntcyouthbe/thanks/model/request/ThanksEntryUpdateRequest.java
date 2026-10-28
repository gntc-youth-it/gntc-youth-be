package com.gntcyouthbe.thanks.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 감사제목만 고친다. 규칙은 올릴 때(ThanksEntryCreateRequest)와 같다.
public record ThanksEntryUpdateRequest(
        @NotBlank(message = "감사제목을 입력해 주세요.")
        @Size(max = 80, message = "감사제목은 80자까지 쓸 수 있어요.")
        String content
) {

    // 검증 전에 앞뒤 공백을 지워서, 공백만 있는 감사제목을 걸러내고 공백을 뺀 길이로 80자를 센다
    public ThanksEntryUpdateRequest {
        if (content != null) {
            content = content.strip();
        }
    }
}
