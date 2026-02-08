package com.gntcyouthbe.user.model.request;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.user.domain.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserProfileRequest {

    @NotBlank(message = "이름은 필수입니다")
    private final String name;

    @NotNull(message = "성전은 필수입니다")
    private final ChurchId churchId;

    @NotNull(message = "기수는 필수입니다")
    @Positive(message = "기수는 양수여야 합니다")
    private final Integer generation;

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
    private final String phoneNumber;

    @NotNull(message = "성별은 필수입니다")
    private final Gender gender;
}
