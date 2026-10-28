package com.gntcyouthbe.thanks.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ThanksCommentCreateRequest(
        @NotBlank(message = "댓글을 입력해 주세요.")
        @Size(max = 100, message = "댓글은 100자까지 쓸 수 있어요.")
        String content
) {

    // 검증 전에 앞뒤 공백을 지워서, 공백만 있는 댓글을 걸러내고 공백을 뺀 길이로 100자를 센다
    public ThanksCommentCreateRequest {
        if (content != null) {
            content = content.strip();
        }
    }
}
