package com.gntcyouthbe.user.model.request;

import com.gntcyouthbe.user.domain.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserRoleUpdateRequest {

    @NotNull(message = "권한은 필수입니다.")
    private final Role role;
}
