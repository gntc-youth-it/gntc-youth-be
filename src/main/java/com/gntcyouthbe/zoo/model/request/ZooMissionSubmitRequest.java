package com.gntcyouthbe.zoo.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ZooMissionSubmitRequest(
        @NotEmpty(message = "답을 입력해 주세요.")
        @Size(max = 10, message = "답은 10개까지 낼 수 있어요.")
        List<@NotNull(message = "답을 입력해 주세요.") @Valid AnswerRequest> answers,

        @NotNull(message = "사진을 올려 주세요.")
        Long photoFileId
) {

    public record AnswerRequest(
            @NotBlank(message = "질문 ID가 비어 있어요.")
            @Size(max = 20, message = "질문 ID는 20자까지 쓸 수 있어요.")
            String questionId,

            @NotBlank(message = "답을 입력해 주세요.")
            @Size(max = 200, message = "답은 200자까지 쓸 수 있어요.")
            String answer
    ) {

        // 검증 전에 앞뒤 공백을 지워서, 공백만 있는 답을 걸러내고 공백을 뺀 길이로 200자를 센다
        public AnswerRequest {
            if (answer != null) {
                answer = answer.strip();
            }
        }
    }
}
