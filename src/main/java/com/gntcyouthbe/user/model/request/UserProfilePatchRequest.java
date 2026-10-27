package com.gntcyouthbe.user.model.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

// 이름과 프로필 사진만 바꾼다. 둘 다 선택이고, 보낸 것만 바꾼다.
public record UserProfilePatchRequest(
        @Size(min = 1, max = 20, message = "이름은 1자에서 20자까지 쓸 수 있어요.")
        String name,

        Long profileImageId
) {

    // 검증 전에 앞뒤 공백을 지워서, 공백만 있는 이름을 걸러내고 공백을 뺀 길이로 20자를 센다
    public UserProfilePatchRequest {
        if (name != null) {
            name = name.strip();
        }
    }

    // 둘 다 없으면 바꿀 것이 없으므로 받지 않는다
    @AssertTrue(message = "바꿀 이름이나 사진을 보내 주세요.")
    public boolean isAnyFieldPresent() {
        return name != null || profileImageId != null;
    }
}
