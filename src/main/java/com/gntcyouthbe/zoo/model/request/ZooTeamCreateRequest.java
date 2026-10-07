package com.gntcyouthbe.zoo.model.request;

import com.gntcyouthbe.zoo.domain.ZooCourse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ZooTeamCreateRequest(
        @NotBlank(message = "조 이름을 입력해 주세요.")
        @Size(max = 20, message = "조 이름은 20자까지 쓸 수 있어요.")
        String name,

        @NotNull(message = "코스를 골라 주세요.")
        ZooCourse course
) {

    // 검증 전에 앞뒤 공백을 지워서, 공백을 뺀 길이로 20자를 센다
    public ZooTeamCreateRequest {
        if (name != null) {
            name = name.strip();
        }
    }
}
